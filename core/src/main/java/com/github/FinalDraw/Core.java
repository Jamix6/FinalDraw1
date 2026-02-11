package com.github.FinalDraw;

import com.badlogic.gdx.Game;

import com.badlogic.gdx.Gdx;
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

    @Override
    public void create() {
        preloadAssets();
        this.setScreen(new Splash());
    }

    private void preloadAssets() {
        Gdx.app.log("Core", "Preloading Assets");

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

        Gdx.app.log("Core", "All assets loaded");
        Gdx.app.log("Splash", "Loading Splash Screen");
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
        if (menuFont != null) menuFont.dispose();
        if (titleFont != null) titleFont.dispose();
        if (bodyFont != null) bodyFont.dispose();

        super.dispose();
    }
}
