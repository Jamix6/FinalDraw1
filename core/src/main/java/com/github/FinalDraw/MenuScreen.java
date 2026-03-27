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
    private static final String[] MENU_ITEMS = {"Play", "Instructions", "Settings", "Exit"};
    private Rectangle[] menuBounds;

    private static final float LEFT_MARGIN = 50;
    private static final float TEXT_SPACING = 60;

    private Texture solidPixel;
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
        easyBounds = new Rectangle(levelPanelBounds.x + 60f, levelPanelBounds.y + 180f, 320f, 45f);
        mediumBounds = new Rectangle(levelPanelBounds.x + 60f, levelPanelBounds.y + 120f, 320f, 45f);
        hardBounds = new Rectangle(levelPanelBounds.x + 60f, levelPanelBounds.y + 60f, 320f, 45f);
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

            if (isHovered) {
                // Draw drop shadow for hovered text (offset by 2 pixels)
                game.menuFont.setColor(0, 0, 0, 0.7f * alpha);
                game.menuFont.draw(batch, MENU_ITEMS[i], LEFT_MARGIN + 2, menuBounds[i].y + menuBounds[i].height - 2);

                // Draw yellow text on top
                game.menuFont.setColor(1f, 1f, 0f, alpha); // Yellow with fade
                game.menuFont.draw(batch, MENU_ITEMS[i], LEFT_MARGIN, menuBounds[i].y + menuBounds[i].height);
            } else {
                // Draw normal white text
                game.menuFont.setColor(1f, 1f, 1f, alpha); // White with fade
                game.menuFont.draw(batch, MENU_ITEMS[i], LEFT_MARGIN, menuBounds[i].y + menuBounds[i].height);
            }
        }

        if (isLevelSelectOpen && solidPixel != null) {
            batch.setColor(0f, 0f, 0f, 0.5f * alpha);
            batch.draw(solidPixel, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setColor(0.12f, 0.12f, 0.12f, 0.95f * alpha);
            batch.draw(solidPixel, levelPanelBounds.x, levelPanelBounds.y, levelPanelBounds.width, levelPanelBounds.height);

            game.titleFont.setColor(1f, 1f, 1f, alpha);
            String title = "SELECT LEVEL";
            layout.setText(game.titleFont, title);
            game.titleFont.draw(batch, title, levelPanelBounds.x + levelPanelBounds.width / 2f - layout.width / 2f, levelPanelBounds.y + levelPanelBounds.height - 20f);

            game.bodyFont.setColor(1f, 1f, 1f, alpha);
            layout.setText(game.bodyFont, "Easy");
            game.bodyFont.draw(batch, "Easy", easyBounds.x, easyBounds.y + 32f);
            layout.setText(game.bodyFont, "Medium");
            game.bodyFont.draw(batch, "Medium", mediumBounds.x, mediumBounds.y + 32f);
            layout.setText(game.bodyFont, "Hard");
            game.bodyFont.draw(batch, "Hard", hardBounds.x, hardBounds.y + 32f);
        }

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
                    game.setScreen(new GameScreen(game));
                    return;
                }
                if (mediumBounds.contains(mouseX, mouseY)) {
                    game.playButtonSfx();
                    game.difficulty = 1;
                    isLevelSelectOpen = false;
                    game.setScreen(new GameScreen(game));
                    return;
                }
                if (hardBounds.contains(mouseX, mouseY)) {
                    game.playButtonSfx();
                    game.difficulty = 2;
                    isLevelSelectOpen = false;
                    game.setScreen(new GameScreen(game));
                    return;
                }
                if (!levelPanelBounds.contains(mouseX, mouseY)) {
                    isLevelSelectOpen = false;
                }
                return;
            }

            for (int i = 0; i < MENU_ITEMS.length; i++) {
                if (menuBounds[i].contains(mouseX, mouseY)) {
                    game.playButtonSfx();
                    handleMenuClick(i);
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
            case 3: Gdx.app.exit(); break;
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

    }
}
