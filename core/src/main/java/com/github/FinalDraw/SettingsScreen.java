package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class SettingsScreen implements Screen {
    private final Core game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private GlyphLayout layout;

    private Rectangle panelBounds;
    private Rectangle backButton;
    private Rectangle musicSliderBounds;
    private Rectangle sfxSliderBounds;
    private Rectangle voiceSliderBounds;
    private int activeSlider = -1;

    public SettingsScreen(Core game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        layout = new GlyphLayout();

        updateButtonPositions();
        game.playMenuMusic();
    }

    private void updateButtonPositions() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        float panelW = Math.min(560f, screenWidth - 80f);
        float panelH = 360f;
        float panelX = screenWidth / 2f - panelW / 2f;
        float panelY = screenHeight / 2f - panelH / 2f;
        panelBounds = new Rectangle(panelX, panelY, panelW, panelH);

        float sliderWidth = panelW - 120f;
        float sliderHeight = 26f;
        float sliderX = panelX + 60f;
        float topY = panelY + panelH - 135f;
        float gap = 80f;
        musicSliderBounds = new Rectangle(sliderX, topY, sliderWidth, sliderHeight);
        sfxSliderBounds = new Rectangle(sliderX, topY - gap, sliderWidth, sliderHeight);
        voiceSliderBounds = new Rectangle(sliderX, topY - gap * 2f, sliderWidth, sliderHeight);

        backButton = new Rectangle(panelX + panelW / 2f - 90f, panelY + 25f, 180f, 50f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();


        batch.draw(game.backgroundStatic, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(game.shadow, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        game.titleFont.setColor(Color.WHITE);
        String header = "SETTINGS";
        layout.setText(game.titleFont, header);
        game.titleFont.draw(batch, header,
            (Gdx.graphics.getWidth() - layout.width) / 2,
            Gdx.graphics.getHeight() - 50);
        batch.end();

        // Draw rounded background panel
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f); // Darker rounded background
        drawRoundedRect(shapeRenderer, panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height, 20f);
        shapeRenderer.end();

        // Draw thick black outline
        Gdx.gl.glLineWidth(3.0f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        drawRoundedRectOutline(shapeRenderer, panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height, 20f);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1.0f);
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        game.titleFont.setColor(Color.YELLOW);
        String subtitle = "AUDIO";
        layout.setText(game.titleFont, subtitle);
        game.titleFont.draw(batch, subtitle, panelBounds.x + panelBounds.width / 2f - layout.width / 2f, panelBounds.y + panelBounds.height - 35f);


        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        drawBackButton(mouseX, mouseY);

        drawAudioSlider("Music", musicSliderBounds, game.getMusicVolume());
        drawAudioSlider("SFX", sfxSliderBounds, game.getSfxVolume());
        drawAudioSlider("Voice", voiceSliderBounds, game.getVoiceVolume());

        batch.end();

        boolean touching = Gdx.input.isTouched();
        if (!touching) {
            activeSlider = -1;
        } else if (activeSlider != -1) {
            updateActiveSlider(mouseX);
        }

        if (Gdx.input.justTouched()) {
            if (musicSliderBounds.contains(mouseX, mouseY)) {
                game.playButtonSfx();
                activeSlider = 0;
                updateActiveSlider(mouseX);
            } else if (sfxSliderBounds.contains(mouseX, mouseY)) {
                game.playButtonSfx();
                activeSlider = 1;
                updateActiveSlider(mouseX);
            } else if (voiceSliderBounds.contains(mouseX, mouseY)) {
                game.playButtonSfx();
                activeSlider = 2;
                updateActiveSlider(mouseX);
            }

            if (backButton.contains(mouseX, mouseY)) {
                game.playButtonSfx();
                returnToMenu();
            }
        }
    }

    private void drawAudioSlider(String labelText, Rectangle bounds, float value) {
        float v = clamp01(value);

        game.bodyFont.setColor(Color.WHITE);
        layout.setText(game.bodyFont, labelText);
        game.bodyFont.draw(batch, labelText, bounds.x, bounds.y + 52f);

        String pct = (int) (v * 100f) + "%";
        layout.setText(game.bodyFont, pct);
        game.bodyFont.draw(batch, pct, bounds.x + bounds.width - layout.width, bounds.y + 52f);
        batch.end();

        // Draw horizontal line and vertical knob using ShapeRenderer
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Background horizontal line (thin)
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
        float lineH = 4f;
        float centerY = bounds.y + bounds.height / 2f;
        shapeRenderer.rect(bounds.x, centerY - lineH / 2f, bounds.width, lineH);

        // Filled part of the line
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(bounds.x, centerY - lineH / 2f, bounds.width * v, lineH);

        // Vertical knob line
        float knobW = 4f;
        float knobH = 30f;
        float knobX = bounds.x + (bounds.width * v) - knobW / 2f;
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(knobX, centerY - knobH / 2f, knobW, knobH);

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
    }

    private void updateActiveSlider(float mouseX) {
        if (activeSlider == 0) {
            game.setMusicVolume(sliderValue(musicSliderBounds, mouseX));
        } else if (activeSlider == 1) {
            game.setSfxVolume(sliderValue(sfxSliderBounds, mouseX));
        } else if (activeSlider == 2) {
            game.setVoiceVolume(sliderValue(voiceSliderBounds, mouseX));
        }
    }

    private float sliderValue(Rectangle bounds, float mouseX) {
        float t = (mouseX - bounds.x) / bounds.width;
        return clamp01(t);
    }

    private float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }

    private void drawBackButton(float mouseX, float mouseY) {
        String buttonText = "Back to Menu";
        boolean isHovered = backButton.contains(mouseX, mouseY);


        batch.setColor(0.2f, 0.2f, 0.2f, 0.8f);
        batch.draw(game.backgroundRectangle,
            backButton.x, backButton.y,
            backButton.width, backButton.height);


        if (isHovered) {

            game.bodyFont.setColor(0, 0, 0, 0.5f);
            layout.setText(game.bodyFont, buttonText);
            game.bodyFont.draw(batch, buttonText,
                backButton.x + (backButton.width - layout.width) / 2 + 2,
                backButton.y + (backButton.height + layout.height) / 2 - 2);


            game.bodyFont.setColor(Color.YELLOW);
            game.bodyFont.draw(batch, buttonText,
                backButton.x + (backButton.width - layout.width) / 2,
                backButton.y + (backButton.height + layout.height) / 2);
        } else {

            game.bodyFont.setColor(Color.WHITE);
            layout.setText(game.bodyFont, buttonText);
            game.bodyFont.draw(batch, buttonText,
                backButton.x + (backButton.width - layout.width) / 2,
                backButton.y + (backButton.height + layout.height) / 2);
        }

        batch.setColor(Color.WHITE);
    }

    private void returnToMenu() {
        game.setScreen(new MenuScreen(game));
    }

    private void drawRoundedRect(ShapeRenderer renderer, float x, float y, float width, float height, float radius) {
        renderer.rect(x + radius, y, width - 2 * radius, height);
        renderer.rect(x, y + radius, radius, height - 2 * radius);
        renderer.rect(x + width - radius, y + radius, radius, height - 2 * radius);
        renderer.arc(x + radius, y + radius, radius, 180, 90);
        renderer.arc(x + width - radius, y + radius, radius, 270, 90);
        renderer.arc(x + width - radius, y + height - radius, radius, 0, 90);
        renderer.arc(x + radius, y + height - radius, radius, 90, 90);
    }

    private void drawRoundedRectOutline(ShapeRenderer renderer, float x, float y, float width, float height, float radius) {
        renderer.line(x + radius, y, x + width - radius, y);
        renderer.line(x + radius, y + height, x + width - radius, y + height);
        renderer.line(x, y + radius, x, y + height - radius);
        renderer.line(x + width, y + radius, x + width, y + height - radius);
        drawArcOnly(renderer, x + radius, y + radius, radius, 180, 90);
        drawArcOnly(renderer, x + width - radius, y + radius, radius, 270, 90);
        drawArcOnly(renderer, x + width - radius, y + height - radius, radius, 0, 90);
        drawArcOnly(renderer, x + radius, y + height - radius, radius, 90, 90);
    }

    private void drawArcOnly(ShapeRenderer renderer, float x, float y, float radius, float start, float degrees) {
        int segments = 20;
        float step = degrees / segments;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) Math.toRadians(start + i * step);
            float angle2 = (float) Math.toRadians(start + (i + 1) * step);
            renderer.line(
                x + (float) Math.cos(angle1) * radius,
                y + (float) Math.sin(angle1) * radius,
                x + (float) Math.cos(angle2) * radius,
                y + (float) Math.sin(angle2) * radius
            );
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width > 0 && height > 0) {
            updateButtonPositions();
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
        if (batch != null) {
            batch.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
