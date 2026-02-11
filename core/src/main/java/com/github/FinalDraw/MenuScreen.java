package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
    private static final String[] MENU_ITEMS = {"Play", "Instructions", "Other", "Exit"};
    private Rectangle[] menuBounds;

    private static final float LEFT_MARGIN = 50;
    private static final float TEXT_SPACING = 60;

    public MenuScreen(Core game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        layout = new GlyphLayout();

        // Initialize fade-in effect
        fadeTimer = 0f;
        alpha = 0f;

        // Create menu bounds
        menuBounds = new Rectangle[MENU_ITEMS.length];
        updateMenuPositions();
    }

    private void updateMenuPositions() {
        float startY = (Gdx.graphics.getHeight() + TEXT_SPACING * (MENU_ITEMS.length - 1)) / 2;

        for (int i = 0; i < MENU_ITEMS.length; i++) {
            layout.setText(game.menuFont, MENU_ITEMS[i]);
            float y = startY - (i * TEXT_SPACING);
            menuBounds[i] = new Rectangle(LEFT_MARGIN, y - layout.height, layout.width, layout.height);
        }
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

        // Reset batch color
        batch.setColor(Color.WHITE);
        batch.end();

        // Handle clicks
        if (Gdx.input.justTouched()) {

            for (int i = 0; i < MENU_ITEMS.length; i++) {
                if (menuBounds[i].contains(mouseX, mouseY)) {
                    handleMenuClick(i);
                    break;
                }
            }
        }
    }

    private void handleMenuClick(int index) {
        switch (index) {
            case 0: System.out.println("Play wala pa"); break;
            case 1: game.setScreen(new Instructions(game)); break;
            case 2: System.out.println("Test Click #3 rar"); break;
            case 3: Gdx.app.exit(); break;
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width > 0 && height > 0) {
            updateMenuPositions();
        }
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();

    }
}
