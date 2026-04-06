package com.github.FinalDraw;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Array;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Core extends Game {

    // Shared assets - preloaded once
    public Array<Texture> backgroundAnimation;
    public Texture backgroundStatic;
    public Texture backgroundRectangle;
    public Texture shadow;
    public Texture logoTexture;
    public Texture splash;
    public BitmapFont menuFont;
    public BitmapFont titleFont;
    public BitmapFont bodyFont;
    public int difficulty = 1;

    private Preferences prefs;
    private float musicVolume;
    private float sfxVolume;
    private float voiceVolume;

    private Music menuMusic;
    private Music[] gameMusic;
    private Music currentMusic;
    private int gameplayMusicIndex;

    private Sound giveCard1Sfx;
    private Sound giveCard2Sfx;
    private Sound givePowerupSfx;
    private Sound buttonSfx;
    @Override
    public void create() {
        preloadAssets();
        this.setScreen(new Splash());
    }

    private void preloadAssets() {
        Gdx.app.log("Core", "Preloading Assets");

        prefs = Gdx.app.getPreferences("FinalDrawSettings");
        musicVolume = clamp01(prefs.getFloat("musicVolume", 1f));
        sfxVolume = clamp01(prefs.getFloat("sfxVolume", 1f));
        voiceVolume = clamp01(prefs.getFloat("voiceVolume", 1f));

        // Load animated background frames
        backgroundAnimation = new Array<>();
        for (int i = 1; i <= 300; i++) {
            String filename = "BG/BACK/ezgif-frame-" + String.format("%03d", i) + ".png";
            try {
                backgroundAnimation.add(new Texture(Gdx.files.internal(filename)));
            } catch (Exception e) {
                break;
            }
        }
        Gdx.app.log("Core", "Loaded a total of " + backgroundAnimation.size + " frames");

        // Load static textures
        try {
            backgroundStatic = new Texture(Gdx.files.internal("BG/Background.png"));
            backgroundRectangle = new Texture(Gdx.files.internal("BG/rectongle.png"));
            shadow = new Texture(Gdx.files.internal("BG/Shadow.png"));
            logoTexture = new Texture(Gdx.files.internal("logo(white).png"));
            splash = new Texture(Gdx.files.internal("splash.png"));
        } catch (Exception e) {
            Gdx.app.error("Core", "Error loading textures", e);
        }

        // Load fonts
        loadFonts();
        loadAudio();

        Gdx.app.log("Core", "All assets loaded");
        Gdx.app.log("Splash", "Loading Splash Screen");
    }

    private void loadAudio() {
        try {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal("Audio/Music/menumusic1 (Seal Core Jazz).mp3"));
            gameMusic = new Music[] {
                Gdx.audio.newMusic(Gdx.files.internal("Audio/Music/music1 (silent_v0_0id).mp3")),
                Gdx.audio.newMusic(Gdx.files.internal("Audio/Music/music2 (FRAGMENTED_CONVERGENCE).mp3")),
                Gdx.audio.newMusic(Gdx.files.internal("Audio/Music/music3 (Breeze).mp3")),
                Gdx.audio.newMusic(Gdx.files.internal("Audio/Music/music4 (for the defeated).mp3")),
                Gdx.audio.newMusic(Gdx.files.internal("Audio/Music/music5 (Finale Of The Unyielding).mp3"))
            };

            giveCard1Sfx = Gdx.audio.newSound(Gdx.files.internal("Audio/SFX/GiveCard1.mp3"));
            giveCard2Sfx = Gdx.audio.newSound(Gdx.files.internal("Audio/SFX/GiveCard2.mp3"));
            givePowerupSfx = Gdx.audio.newSound(Gdx.files.internal("Audio/SFX/GivePowerup.mp3"));
            buttonSfx = Gdx.audio.newSound(Gdx.files.internal("Audio/SFX/HoverButtonMenu.mp3"));
            Gdx.app.log("Audio", "Loaded all Audios");
        } catch (Exception e) {
            Gdx.app.error("Core", "Error loading audio", e);
        }
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public float getVoiceVolume() {
        return voiceVolume;
    }

    public void setMusicVolume(float volume) {
        musicVolume = clamp01(volume);
        if (prefs != null) {
            prefs.putFloat("musicVolume", musicVolume);
            prefs.flush();
        }
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }

    public void setSfxVolume(float volume) {
        sfxVolume = clamp01(volume);
        if (prefs != null) {
            prefs.putFloat("sfxVolume", sfxVolume);
            prefs.flush();
        }
    }

    public void setVoiceVolume(float volume) {
        voiceVolume = clamp01(volume);
        if (prefs != null) {
            prefs.putFloat("voiceVolume", voiceVolume);
            prefs.flush();
        }
    }

    public void playMenuMusic() {
        if (menuMusic == null) return;
        if (currentMusic == menuMusic && currentMusic.isPlaying()) {
            currentMusic.setVolume(musicVolume);
            return;
        }
        stopCurrentMusic();
        currentMusic = menuMusic;
        currentMusic.setLooping(true);
        currentMusic.setOnCompletionListener(null);
        currentMusic.setVolume(musicVolume);
        currentMusic.play();
    }

    public void startGameplayMusic() {
        if (gameMusic == null || gameMusic.length == 0) return;
        gameplayMusicIndex = (int) (Math.random() * gameMusic.length);
        playGameplayTrack(gameplayMusicIndex);
    }

    private void playGameplayTrack(int index) {
        if (gameMusic == null || gameMusic.length == 0) return;
        if (index < 0) index = 0;
        if (index >= gameMusic.length) index = gameMusic.length - 1;
        Music track = gameMusic[index];
        if (track == null) return;

        stopCurrentMusic();
        currentMusic = track;
        currentMusic.setVolume(musicVolume);
        boolean isLast = index == gameMusic.length - 1;
        currentMusic.setLooping(isLast);

        if (!isLast) {
            final Music expectedTrack = track;
            final int expectedIndex = index;
            currentMusic.setOnCompletionListener(music -> {
                if (currentMusic != expectedTrack) return;
                Gdx.app.postRunnable(() -> {
                    if (currentMusic != expectedTrack) return;
                    gameplayMusicIndex = Math.min(gameMusic.length - 1, expectedIndex + 1);
                    playGameplayTrack(gameplayMusicIndex);
                });
            });
        } else {
            currentMusic.setOnCompletionListener(null);
        }

        currentMusic.play();
    }

    public void playGameMusicForProgress(int progress) {
        if (gameMusic == null || gameMusic.length == 0) return;
        int idx = progress - 1;
        if (idx < 0) idx = 0;
        if (idx >= gameMusic.length) idx = gameMusic.length - 1;
        gameplayMusicIndex = idx;
        playGameplayTrack(gameplayMusicIndex);
    }

    public void stopCurrentMusic() {
        if (currentMusic != null) {
            currentMusic.setOnCompletionListener(null);
            currentMusic.stop();
            currentMusic = null;
        }
    }

    public void playCardSfx() {
        Sound sfx = (Math.random() < 0.5) ? giveCard1Sfx : giveCard2Sfx;
        if (sfx != null) sfx.play(sfxVolume);
    }

    public void playPowerupSfx() {
        if (givePowerupSfx != null) givePowerupSfx.play(sfxVolume);
    }

    public void playButtonSfx() {
        if (buttonSfx != null) buttonSfx.play(sfxVolume);
    }

    private float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }

    private void loadFonts() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/AgencyFB.ttf"));

        // Menu font (36pt)
        FreeTypeFontParameter menuParam = new FreeTypeFontParameter();
        menuParam.size = 36;
        menuParam.color = Color.WHITE;
        menuParam.minFilter = Texture.TextureFilter.Linear;
        menuParam.magFilter = Texture.TextureFilter.Linear;
        menuFont = generator.generateFont(menuParam);

        // Title font (48pt)
        FreeTypeFontParameter titleParam = new FreeTypeFontParameter();
        titleParam.size = 48;
        titleParam.color = Color.WHITE;
        titleParam.minFilter = Texture.TextureFilter.Linear;
        titleParam.magFilter = Texture.TextureFilter.Linear;
        titleFont = generator.generateFont(titleParam);

        // Body font (24pt)
        FreeTypeFontParameter bodyParam = new FreeTypeFontParameter();
        bodyParam.size = 24;
        bodyParam.color = Color.WHITE;
        bodyParam.minFilter = Texture.TextureFilter.Linear;
        bodyParam.magFilter = Texture.TextureFilter.Linear;
        bodyFont = generator.generateFont(bodyParam);

        generator.dispose();
    }

    @Override
    public void dispose() {
        // Dispose all shared assets
        if (backgroundAnimation != null) {
            for (Texture frame : backgroundAnimation) {
                frame.dispose();
            }
        }
        if (backgroundStatic != null) backgroundStatic.dispose();
        if (shadow != null) shadow.dispose();
        if (logoTexture != null) logoTexture.dispose();
        if (splash != null) splash.dispose();
        if (menuFont != null) menuFont.dispose();
        if (titleFont != null) titleFont.dispose();
        if (bodyFont != null) bodyFont.dispose();
        stopCurrentMusic();
        if (menuMusic != null) menuMusic.dispose();
        if (gameMusic != null) {
            for (Music m : gameMusic) {
                if (m != null) m.dispose();
            }
        }
        if (giveCard1Sfx != null) giveCard1Sfx.dispose();
        if (giveCard2Sfx != null) giveCard2Sfx.dispose();
        if (givePowerupSfx != null) givePowerupSfx.dispose();
        if (buttonSfx != null) buttonSfx.dispose();

        super.dispose();
    }

}
