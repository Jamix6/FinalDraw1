package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class SettingsScreen implements Screen {
    private final Core game;
    private SpriteBatch batch;
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

        batch.setColor(0f, 0f, 0f, 0.55f);
        batch.draw(game.backgroundRectangle, panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height);
        batch.setColor(Color.WHITE);

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

        batch.setColor(0.15f, 0.15f, 0.15f, 0.85f);
        batch.draw(game.backgroundRectangle, bounds.x, bounds.y, bounds.width, bounds.height);

        float filledW = bounds.width * v;
        batch.setColor(1f, 0.84f, 0f, 0.85f);
        batch.draw(game.backgroundRectangle, bounds.x, bounds.y, filledW, bounds.height);

        float knobW = 18f;
        float knobH = bounds.height + 12f;
        float knobX = bounds.x + filledW - knobW / 2f;
        float knobY = bounds.y - (knobH - bounds.height) / 2f;
        batch.setColor(Color.WHITE);
        batch.draw(game.backgroundRectangle, knobX, knobY, knobW, knobH);

        batch.setColor(Color.WHITE);
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
    }
}
