package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Splash implements Screen {
    private Core game;
    private SpriteBatch batch;

    private float splashTimer;
    private float alpha;
    private boolean fadeOut;

    private static final float SPLASH_DURATION = 3.0f; // Total TIme
    private static final float FADE_DURATION = 1.0f;  // Fade out

    public Splash() {
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        splashTimer = 0f;
        alpha = 0f;
        fadeOut = false;

        // Get Core reference now that it's initialized
        game = (Core) Gdx.app.getApplicationListener();
    }

    @Override
    public void render(float delta) {
        splashTimer += delta;

        // Calculate alpha for fade effects
        if (!fadeOut) {
            // Fade in phase
            if (splashTimer < FADE_DURATION) {
                alpha = splashTimer / FADE_DURATION;
            } else {
                alpha = 1f;
                // Start fading out after displaying for a while
                if (splashTimer > SPLASH_DURATION - FADE_DURATION) {
                    fadeOut = true;
                }
            }
        } else {
            // Fade out phase
            float fadeOutProgress = (splashTimer - (SPLASH_DURATION - FADE_DURATION)) / FADE_DURATION;
            alpha = 1f - fadeOutProgress;

            // Transition to MenuScreen when fade out is complete
            if (alpha <= 0f && game != null) {
                game.setScreen(new MenuScreen(game));
                return;
            }
        }

        // Clear screen with black background
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Draw logo in center with fade effect
        if (game != null && game.splash != null) {
            batch.setColor(1f, 1f, 1f, alpha);
            float logoSize = 400;
            float x = (Gdx.graphics.getWidth() - logoSize) / 2;
            float y = (Gdx.graphics.getHeight() - logoSize) / 2;
            batch.draw(game.splash, x, y, logoSize, logoSize);
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {}

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
