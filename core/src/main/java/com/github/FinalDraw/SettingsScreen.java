package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;

public class SettingsScreen implements Screen {
    private final Core game;
    private SpriteBatch batch;
    private GlyphLayout layout;

    //Diff options
    private static final String[] DIFFICULTIES = {"Easy", "Medium", "Hard"};
    private static final String[] DIFFICULTY_DESCRIPTIONS = {
        "Player: 5 lives | AI: 5 lives",
        "Player: 5 lives | AI: 7 lives",
        "Player: 5 lives | AI: 11 lives"
    };

    // Current selected difficulty (stored in Core)
    private int selectedDifficulty;


    private Rectangle[] difficultyButtons;
    private Rectangle backButton;

    // Diff values
    public static final int PLAYER_LIVES = 5;
    public static final int[] AI_LIVES = {5, 7, 11};

    public SettingsScreen(Core game) {
        this.game = game;
        // Load current difficulty from Core (default to Medium/1 if not set)
        this.selectedDifficulty = game.difficulty; // No need for -1 check now
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        layout = new GlyphLayout();

        updateButtonPositions();
    }

    private void updateButtonPositions() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();


        difficultyButtons = new Rectangle[DIFFICULTIES.length];
        float buttonStartY = screenHeight / 2 + 100;
        float buttonSpacing = 120;

        for (int i = 0; i < DIFFICULTIES.length; i++) {
            float buttonY = buttonStartY - (i * buttonSpacing);
            difficultyButtons[i] = new Rectangle(screenWidth/2 - 150, buttonY, 300, 80);
        }


        backButton = new Rectangle(screenWidth/2 - 75, 80, 150, 50);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();


        batch.draw(game.backgroundStatic, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(game.shadow, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());


        game.titleFont.setColor(Color.WHITE);
        layout.setText(game.titleFont, "SETTINGS");
        game.titleFont.draw(batch, "SETTINGS",
            (Gdx.graphics.getWidth() - layout.width) / 2,
            Gdx.graphics.getHeight() - 50);


        game.bodyFont.setColor(Color.YELLOW);
        String subtitle = "Select Difficulty";
        layout.setText(game.bodyFont, subtitle);
        game.bodyFont.draw(batch, subtitle,
            (Gdx.graphics.getWidth() - layout.width) / 2,
            Gdx.graphics.getHeight() - 120);


        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();


        for (int i = 0; i < DIFFICULTIES.length; i++) {
            drawDifficultyButton(i, mouseX, mouseY);
        }


        drawBackButton(mouseX, mouseY);


        game.bodyFont.setColor(Color.CYAN);
        String currentSetting = "Current: " + DIFFICULTIES[selectedDifficulty];
        layout.setText(game.bodyFont, currentSetting);
        game.bodyFont.draw(batch, currentSetting,
            (Gdx.graphics.getWidth() - layout.width) / 2,
            200);


        game.bodyFont.setColor(Color.LIGHT_GRAY);
        String livesInfo = "Player: 5 lives | AI: " + AI_LIVES[selectedDifficulty] + " lives";
        layout.setText(game.bodyFont, livesInfo);
        game.bodyFont.draw(batch, livesInfo,
            (Gdx.graphics.getWidth() - layout.width) / 2,
            160);

        batch.end();


        if (Gdx.input.justTouched()) {
            // Check difficulty buttons
            for (int i = 0; i < difficultyButtons.length; i++) {
                if (difficultyButtons[i].contains(mouseX, mouseY)) {
                    selectedDifficulty = i;
                    game.difficulty = i; // Save to Core
                    break;
                }
            }


            if (backButton.contains(mouseX, mouseY)) {
                returnToMenu();
            }
        }
    }

    private void drawDifficultyButton(int index, float mouseX, float mouseY) {
        String difficultyName = DIFFICULTIES[index];
        String description = DIFFICULTY_DESCRIPTIONS[index];
        Rectangle bounds = difficultyButtons[index];
        boolean isSelected = (index == selectedDifficulty);
        boolean isHovered = bounds.contains(mouseX, mouseY);

        // Draw button background
        if (isSelected) {
            // Selected button (gold background)
            batch.setColor(1f, 0.84f, 0f, 0.8f); // Gold color
        } else if (isHovered) {
            // Hovered button (light gray background)
            batch.setColor(0.6f, 0.6f, 0.6f, 0.8f);
        } else {
            // Normal button (dark gray background)
            batch.setColor(0.3f, 0.3f, 0.3f, 0.8f);
        }

        batch.draw(game.backgroundRectangle,
            bounds.x, bounds.y, bounds.width, bounds.height);


        if (isSelected) {
            game.titleFont.setColor(Color.BLACK);
        } else if (isHovered) {
            game.titleFont.setColor(Color.YELLOW);
        } else {
            game.titleFont.setColor(Color.WHITE);
        }

        layout.setText(game.titleFont, difficultyName);
        game.titleFont.draw(batch, difficultyName,
            bounds.x + (bounds.width - layout.width) / 2,
            bounds.y + bounds.height - 5); // Changed from -20 to -25 (moved up)


        if (isSelected) {
            game.bodyFont.setColor(Color.BLACK);
        } else if (isHovered) {
            game.bodyFont.setColor(Color.LIGHT_GRAY);
        } else {
            game.bodyFont.setColor(0.8f, 0.8f, 0.8f, 1f);
        }

        layout.setText(game.bodyFont, description);
        game.bodyFont.draw(batch, description,
            bounds.x + (bounds.width - layout.width) / 2,
            bounds.y + 25); // Changed from 30 to 25 (moved down)



        batch.setColor(Color.WHITE);
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
