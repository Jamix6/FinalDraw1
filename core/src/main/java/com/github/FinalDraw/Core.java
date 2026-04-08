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
    public static final int MAX_STAGES = 5;
    public static final int MAX_PROFILES = 10;
    public static final int MAX_NAME_LENGTH = 12;
    public static final int MIN_NAME_LENGTH = 1;

    private Preferences prefs;
    private float musicVolume;
    private float sfxVolume;
    private float voiceVolume;
    private int currentProfileSlot = -1; // -1 means no profile selected

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

    // Legacy stage progression methods (kept for backward compatibility during migration)
    // These will be removed after migration is complete
    private int getLegacyUnlockedStage() {
        if (prefs == null) {
            prefs = Gdx.app.getPreferences("FinalDrawSettings");
        }
        return prefs.getInteger("unlockedStage", 1);
    }

    private void setLegacyUnlockedStage(int stage) {
        if (prefs == null) {
            prefs = Gdx.app.getPreferences("FinalDrawSettings");
        }
        stage = Math.max(1, Math.min(stage, MAX_STAGES));
        prefs.putInteger("unlockedStage", stage);
        prefs.flush();
    }

    private void completeLegacyStage(int stage) {
        if (prefs == null) {
            prefs = Gdx.app.getPreferences("FinalDrawSettings");
        }
        int currentUnlocked = getLegacyUnlockedStage();
        if (stage >= currentUnlocked && stage < MAX_STAGES) {
            setLegacyUnlockedStage(stage + 1);
        }
    }

    public void resetProgression() {
        // Reset current profile's progression for all difficulties
        SaveProfile profile = getCurrentProfile();
        if (profile != null) {
            profile.easyUnlocked = 1;
            profile.mediumUnlocked = 1;
            profile.hardUnlocked = 1;
            saveProfile(profile);
        }
    }

    // ============================================
    // NEW: Profile Management System
    // ============================================

    public static class SaveProfile {
        public int slot;
        public String name;
        public String createdDate;
        public String lastPlayedDate;
        public long playTimeMinutes;
        public int easyUnlocked;
        public int mediumUnlocked;
        public int hardUnlocked;
        public int mediumLives; // Lives for Medium difficulty retry system
        
        public SaveProfile(int slot) {
            this.slot = slot;
            this.name = "";
            this.createdDate = "";
            this.lastPlayedDate = "";
            this.playTimeMinutes = 0;
            this.easyUnlocked = 1;
            this.mediumUnlocked = 1;
            this.hardUnlocked = 1;
            this.mediumLives = 3; // Start with 3 lives for Medium difficulty
        }
        
        public boolean exists() {
            return name != null && !name.isEmpty();
        }
        
        public int getUnlockedStage(int difficulty) {
            switch (difficulty) {
                case 0: return easyUnlocked;
                case 1: return mediumUnlocked;
                case 2: return hardUnlocked;
                default: return 1;
            }
        }
        
        public void setUnlockedStage(int difficulty, int stage) {
            stage = Math.max(1, Math.min(stage, MAX_STAGES));
            switch (difficulty) {
                case 0: easyUnlocked = stage; break;
                case 1: mediumUnlocked = stage; break;
                case 2: hardUnlocked = stage; break;
            }
        }
        
        public void completeStage(int difficulty, int stage) {
            int current = getUnlockedStage(difficulty);
            if (stage >= current && stage < MAX_STAGES) {
                setUnlockedStage(difficulty, stage + 1);
            }
        }
        
        public int getMediumLives() {
            return mediumLives;
        }
        
        public void setMediumLives(int lives) {
            this.mediumLives = Math.max(0, lives);
        }
        
        public void decrementMediumLives() {
            if (mediumLives > 0) {
                mediumLives--;
            }
        }
        
        public void resetMediumProgression() {
            mediumUnlocked = 1;
            mediumLives = 3;
        }
        
        public String getFormattedPlayTime() {
            long hours = playTimeMinutes / 60;
            long minutes = playTimeMinutes % 60;
            if (hours > 0) {
                return String.format("%dh %dm", hours, minutes);
            } else {
                return String.format("%dm", minutes);
            }
        }
        
        public String getFormattedLastPlayed() {
            if (lastPlayedDate == null || lastPlayedDate.isEmpty()) {
                return "Never";
            }
            try {
                // Simple formatting: just show date part
                if (lastPlayedDate.length() >= 10) {
                    return lastPlayedDate.substring(0, 10);
                }
                return lastPlayedDate;
            } catch (Exception e) {
                return "Unknown";
            }
        }
        
        public String getDifficultySummary() {
            return String.format("Easy: %d, Medium: %d, Hard: %d", 
                easyUnlocked, mediumUnlocked, hardUnlocked);
        }
    }

    // Profile management methods
    public int getCurrentProfileSlot() {
        return currentProfileSlot;
    }
    
    public void setCurrentProfileSlot(int slot) {
        if (slot >= 1 && slot <= MAX_PROFILES) {
            currentProfileSlot = slot;
            // Update last played date
            SaveProfile profile = loadProfile(slot);
            if (profile != null && profile.exists()) {
                profile.lastPlayedDate = getCurrentTimestamp();
                saveProfile(profile);
            }
        }
    }
    
    public SaveProfile getCurrentProfile() {
        if (currentProfileSlot <= 0 || currentProfileSlot > MAX_PROFILES) {
            return null;
        }
        return loadProfile(currentProfileSlot);
    }
    
    public SaveProfile loadProfile(int slot) {
        if (slot < 1 || slot > MAX_PROFILES) return null;
        
        SaveProfile profile = new SaveProfile(slot);
        String prefix = "profile" + slot + "_";
        
        profile.name = prefs.getString(prefix + "name", "");
        profile.createdDate = prefs.getString(prefix + "created", "");
        profile.lastPlayedDate = prefs.getString(prefix + "lastPlayed", "");
        profile.playTimeMinutes = prefs.getLong(prefix + "playTime", 0);
        profile.easyUnlocked = prefs.getInteger(prefix + "easyUnlocked", 1);
        profile.mediumUnlocked = prefs.getInteger(prefix + "mediumUnlocked", 1);
        profile.hardUnlocked = prefs.getInteger(prefix + "hardUnlocked", 1);
        profile.mediumLives = prefs.getInteger(prefix + "mediumLives", 3);
        
        return profile;
    }
    
    public void saveProfile(SaveProfile profile) {
        if (profile == null || profile.slot < 1 || profile.slot > MAX_PROFILES) return;
        
        String prefix = "profile" + profile.slot + "_";
        
        prefs.putString(prefix + "name", profile.name);
        prefs.putString(prefix + "created", profile.createdDate);
        prefs.putString(prefix + "lastPlayed", profile.lastPlayedDate);
        prefs.putLong(prefix + "playTime", profile.playTimeMinutes);
        prefs.putInteger(prefix + "easyUnlocked", profile.easyUnlocked);
        prefs.putInteger(prefix + "mediumUnlocked", profile.mediumUnlocked);
        prefs.putInteger(prefix + "hardUnlocked", profile.hardUnlocked);
        prefs.putInteger(prefix + "mediumLives", profile.mediumLives);
        prefs.putBoolean(prefix + "exists", profile.exists());
        
        prefs.flush();
    }
    
    public SaveProfile createProfile(int slot, String name) {
        if (slot < 1 || slot > MAX_PROFILES) return null;
        if (name == null || name.trim().isEmpty()) return null;
        if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH) return null;
        
        SaveProfile profile = new SaveProfile(slot);
        profile.name = name.trim();
        profile.createdDate = getCurrentTimestamp();
        profile.lastPlayedDate = profile.createdDate;
        
        // Check for legacy migration
        migrateLegacyProgression(profile);
        
        saveProfile(profile);
        return profile;
    }
    
    public boolean deleteProfile(int slot) {
        if (slot < 1 || slot > MAX_PROFILES) return false;
        
        String prefix = "profile" + slot + "_";
        prefs.remove(prefix + "name");
        prefs.remove(prefix + "created");
        prefs.remove(prefix + "lastPlayed");
        prefs.remove(prefix + "playTime");
        prefs.remove(prefix + "easyUnlocked");
        prefs.remove(prefix + "mediumUnlocked");
        prefs.remove(prefix + "hardUnlocked");
        prefs.remove(prefix + "mediumLives");
        prefs.remove(prefix + "exists");
        
        prefs.flush();
        
        if (currentProfileSlot == slot) {
            currentProfileSlot = -1;
        }
        
        return true;
    }
    
    public Array<SaveProfile> getAllProfiles() {
        Array<SaveProfile> profiles = new Array<>();
        for (int i = 1; i <= MAX_PROFILES; i++) {
            SaveProfile profile = loadProfile(i);
            profiles.add(profile);
        }
        return profiles;
    }
    
    private void migrateLegacyProgression(SaveProfile profile) {
        // Check if there's old progression data to migrate
        if (prefs.contains("unlockedStage")) {
            int legacyStage = prefs.getInteger("unlockedStage", 1);
            // Apply to all difficulties
            profile.easyUnlocked = legacyStage;
            profile.mediumUnlocked = legacyStage;
            profile.hardUnlocked = legacyStage;
            
            // Clear old data
            prefs.remove("unlockedStage");
            prefs.flush();
            
            Gdx.app.log("Core", "Migrated legacy progression stage " + legacyStage + " to profile " + profile.slot);
        }
    }
    
    private String getCurrentTimestamp() {
        // Simple ISO-like timestamp
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return sdf.format(new java.util.Date());
    }
    
    // Difficulty-specific progression methods (for current profile)
    public int getUnlockedStage(int difficulty) {
        SaveProfile profile = getCurrentProfile();
        if (profile == null) return 1;
        return profile.getUnlockedStage(difficulty);
    }
    
    public void setUnlockedStage(int difficulty, int stage) {
        SaveProfile profile = getCurrentProfile();
        if (profile == null) return;
        profile.setUnlockedStage(difficulty, stage);
        saveProfile(profile);
    }
    
    public void completeStage(int difficulty, int stage) {
        SaveProfile profile = getCurrentProfile();
        if (profile == null) return;
        profile.completeStage(difficulty, stage);
        saveProfile(profile);
    }
    
    public int getMediumLives() {
        SaveProfile profile = getCurrentProfile();
        if (profile == null) return 3;
        return profile.getMediumLives();
    }
    
    public void setMediumLives(int lives) {
        SaveProfile profile = getCurrentProfile();
        if (profile == null) return;
        profile.setMediumLives(lives);
        saveProfile(profile);
    }
    
    public void decrementMediumLives() {
        SaveProfile profile = getCurrentProfile();
        if (profile == null) return;
        profile.decrementMediumLives();
        saveProfile(profile);
    }
    
    public void resetMediumProgression() {
        SaveProfile profile = getCurrentProfile();
        if (profile == null) return;
        profile.resetMediumProgression();
        saveProfile(profile);
    }
    
    public void updatePlayTime(long minutes) {
        SaveProfile profile = getCurrentProfile();
        if (profile == null) return;
        profile.playTimeMinutes += minutes;
        saveProfile(profile);
    }
    
    // Backward compatibility wrapper (uses current profile's medium difficulty)
    public int getUnlockedStage() {
        return getUnlockedStage(1); // Default to medium
    }
    
    public void setUnlockedStage(int stage) {
        setUnlockedStage(1, stage); // Default to medium
    }
    
    public void completeStage(int stage) {
        completeStage(1, stage); // Default to medium
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
