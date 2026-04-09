package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class MenuScreen implements Screen {
    private final Core game;
    private SpriteBatch batch;
    private GlyphLayout layout;
    private float animTimer;
    private static final float FRAME_TIME = 0.1f;

    // Fade in effect
    private float fadeTimer;
    private float alpha;
    private static final float FADE_IN_DURATION = 1.0f;

    // Menu items
    private static final String[] MENU_ITEMS = {"Play", "Instructions", "Settings", "Character Select", "Exit"};
    private Rectangle[] menuBounds;

    private static final float LEFT_MARGIN = 50;
    private static final float TEXT_SPACING = 60;

    private Texture solidPixel;
    private Texture difficultyPanelTexture;
    private boolean isLevelSelectOpen;
    private Rectangle levelPanelBounds;
    private Rectangle easyBounds;
    private Rectangle mediumBounds;
    private Rectangle hardBounds;

    public MenuScreen(Core game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        layout = new GlyphLayout();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        solidPixel = new Texture(pixmap);
        pixmap.dispose();

        // Load difficulty panel texture
        if (Gdx.files.internal("Panels/diffpanel.png").exists()) {
            difficultyPanelTexture = new Texture(Gdx.files.internal("Panels/diffpanel.png"));
        } else {
            difficultyPanelTexture = null;
        }

        // Initialize fade-in effect
        fadeTimer = 0f;
        alpha = 0f;

        // Create menu bounds
        menuBounds = new Rectangle[MENU_ITEMS.length];
        updateMenuPositions();
        updateLevelSelectPositions();

        game.playMenuMusic();
    }

    private void updateMenuPositions() {
        float startY = (Gdx.graphics.getHeight() + TEXT_SPACING * (MENU_ITEMS.length - 1)) / 2;

        for (int i = 0; i < MENU_ITEMS.length; i++) {
            layout.setText(game.menuFont, MENU_ITEMS[i]);
            float y = startY - (i * TEXT_SPACING);
            menuBounds[i] = new Rectangle(LEFT_MARGIN, y - layout.height, layout.width, layout.height);
        }
    }

    private void updateLevelSelectPositions() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        levelPanelBounds = new Rectangle(w / 2f - 220f, h / 2f - 140f, 440f, 280f);
        easyBounds = new Rectangle(levelPanelBounds.x + 100f, levelPanelBounds.y + 170f, 240f, 30f);
        mediumBounds = new Rectangle(levelPanelBounds.x + 100f, levelPanelBounds.y + 105f, 240f, 30f);
        hardBounds = new Rectangle(levelPanelBounds.x + 100f, levelPanelBounds.y + 40f, 240f, 30f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        animTimer += delta;

        // Update fade timer
        fadeTimer += delta;
        alpha = Math.min(fadeTimer / FADE_IN_DURATION, 1);

        batch.begin();

        // Apply fade-in effect to all elements
        batch.setColor(1f, 1f, 1f, alpha);

        // Background Vid
        if (game.backgroundAnimation.size > 0) {
            int frame = (int)(animTimer / FRAME_TIME) % game.backgroundAnimation.size;
            batch.draw(game.backgroundAnimation.get(frame), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        // Shadow
        batch.draw(game.shadow, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Draw logo
        float logoSize = 300;
        batch.draw(game.logoTexture, 20, Gdx.graphics.getHeight() - logoSize + 20, logoSize, logoSize);

        // Calculate mouse position once for both hover and click detection
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        // Draw menu items with hover effects
        for (int i = 0; i < MENU_ITEMS.length; i++) {
            boolean isHovered = menuBounds[i].contains(mouseX, mouseY);
            boolean isEnabled = true;

            // Check if "Play" button should be disabled (index 0 is "Play")
            if (i == 0) { // "Play" button
                isEnabled = game.getCurrentProfile() != null;
            }

            if (isEnabled && isHovered) {
                // Draw drop shadow for hovered text (offset by 2 pixels)
                game.menuFont.setColor(0, 0, 0, 0.7f * alpha);
                game.menuFont.draw(batch, MENU_ITEMS[i], LEFT_MARGIN + 2, menuBounds[i].y + menuBounds[i].height - 2);

                // Draw yellow text on top
                game.menuFont.setColor(1f, 1f, 0f, alpha); // Yellow with fade
                game.menuFont.draw(batch, MENU_ITEMS[i], LEFT_MARGIN, menuBounds[i].y + menuBounds[i].height);
            } else if (isEnabled) {
                // Draw normal white text for enabled items
                game.menuFont.setColor(1f, 1f, 1f, alpha); // White with fade
                game.menuFont.draw(batch, MENU_ITEMS[i], LEFT_MARGIN, menuBounds[i].y + menuBounds[i].height);
            } else {
                // Draw disabled (grayed out) text
                game.menuFont.setColor(0.5f, 0.5f, 0.5f, alpha); // Gray with fade
                game.menuFont.draw(batch, MENU_ITEMS[i], LEFT_MARGIN, menuBounds[i].y + menuBounds[i].height);
            }
        }

        if (isLevelSelectOpen && solidPixel != null) {
            batch.setColor(0f, 0f, 0f, 0.5f * alpha);
            batch.draw(solidPixel, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setColor(Color.WHITE);

            // Draw difficulty panel texture
            if (difficultyPanelTexture != null) {
                batch.draw(difficultyPanelTexture, levelPanelBounds.x, levelPanelBounds.y, levelPanelBounds.width, levelPanelBounds.height);
            }

            /* Draw debug rectangles for button click areas (can be removed)
            batch.setColor(1f, 0f, 0f, 0.2f);
            batch.draw(solidPixel, easyBounds.x, easyBounds.y, easyBounds.width, easyBounds.height);
            batch.draw(solidPixel, mediumBounds.x, mediumBounds.y, mediumBounds.width, mediumBounds.height);
            batch.draw(solidPixel, hardBounds.x, hardBounds.y, hardBounds.width, hardBounds.height);
            batch.setColor(Color.WHITE);

             */
        }

        // Music Credit Rawr
        game.bodyFont.setColor(1f, 1f, 1f, 0.1f * alpha);
        layout.setText(game.bodyFont, "Music by Xeolt");
        game.bodyFont.draw(batch, layout, Gdx.graphics.getWidth() - layout.width - 1140, 30);

        // Reset batch color
        batch.setColor(Color.WHITE);
        batch.end();

        if (isLevelSelectOpen && Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            isLevelSelectOpen = false;
        }

        // Handle clicks
        if (Gdx.input.justTouched()) {
            if (isLevelSelectOpen) {
                if (easyBounds.contains(mouseX, mouseY)) {
                    game.playButtonSfx();
                    game.difficulty = 0;
                    isLevelSelectOpen = false;
                    game.setScreen(new StageSelectScreen(game));
                    return;
                }
                if (mediumBounds.contains(mouseX, mouseY)) {
                    game.playButtonSfx();
                    game.difficulty = 1;
                    isLevelSelectOpen = false;
                    game.setScreen(new StageSelectScreen(game));
                    return;
                }
                if (hardBounds.contains(mouseX, mouseY)) {
                    game.playButtonSfx();
                    game.difficulty = 2;
                    isLevelSelectOpen = false;
                    game.setScreen(new StageSelectScreen(game));
                    return;
                }
                if (!levelPanelBounds.contains(mouseX, mouseY)) {
                    isLevelSelectOpen = false;
                }
                return;
            }

            for (int i = 0; i < MENU_ITEMS.length; i++) {
                if (menuBounds[i].contains(mouseX, mouseY)) {
                    // Check if "Play" button is enabled
                    boolean isEnabled = true;
                    if (i == 0) { // "Play" button
                        isEnabled = game.getCurrentProfile() != null;
                    }

                    if (isEnabled) {
                        game.playButtonSfx();
                        handleMenuClick(i);
                    }
                    break;
                }
            }
        }
    }

    private void handleMenuClick(int index) {
        switch (index) {
            case 0: isLevelSelectOpen = true; break;
            case 1: game.setScreen(new Instructions(game)); break;
            case 2: game.setScreen(new SettingsScreen(game)); break;
            case 3: game.setScreen(new CharacterSelectScreen(game)); break;
            case 4: Gdx.app.exit(); break;
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width > 0 && height > 0) {
            updateMenuPositions();
            updateLevelSelectPositions();
        }
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (solidPixel != null) {
            solidPixel.dispose();
            solidPixel = null;
        }
        if (difficultyPanelTexture != null) {
            difficultyPanelTexture.dispose();
            difficultyPanelTexture = null;
        }

    }
}
