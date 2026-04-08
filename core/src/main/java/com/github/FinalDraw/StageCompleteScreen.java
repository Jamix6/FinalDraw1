package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class StageCompleteScreen implements Screen {
    private final Core game;
    private SpriteBatch batch;
    private GlyphLayout layout;
    
    private int stage;
    private boolean won;
    private int difficulty;
    
    // Buttons
    private Rectangle continueButton;
    private Rectangle retryButton;
    private Rectangle stageSelectButton;
    
    // Roman numerals
    private static final String[] ROMAN_NUMERALS = {"I", "II", "III", "IV", "V"};
    
    public StageCompleteScreen(Core game, int stage, boolean won) {
        this.game = game;
        this.stage = stage;
        this.won = won;
        this.difficulty = game.difficulty;
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
        
        float centerX = screenWidth / 2f;
        float buttonWidth = 300f;
        float buttonHeight = 50f;
        float buttonSpacing = 20f;
        
        float startY = screenHeight / 2f - 100f;
        
        if (won) {
            // Win: Continue, Stage Select, Retry
            continueButton = new Rectangle(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight);
            stageSelectButton = new Rectangle(centerX - buttonWidth / 2, startY - buttonHeight - buttonSpacing, buttonWidth, buttonHeight);
            retryButton = new Rectangle(centerX - buttonWidth / 2, startY - 2 * (buttonHeight + buttonSpacing), buttonWidth, buttonHeight);
        } else {
            // Lose: Check difficulty for retry option
            if (difficulty == 2) { // Hard - no retry, reset progression
                game.resetProgression();
                retryButton = null;
                stageSelectButton = new Rectangle(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight);
                continueButton = null;
            } else if (difficulty == 1) { // Medium difficulty
                int livesLeft = game.getMediumLives();
                if (livesLeft > 0) {
                    // Still have lives, allow retry
                    retryButton = new Rectangle(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight);
                    stageSelectButton = new Rectangle(centerX - buttonWidth / 2, startY - buttonHeight - buttonSpacing, buttonWidth, buttonHeight);
                } else {
                    // No lives left, reset progression and only show stage select
                    game.resetMediumProgression();
                    retryButton = null;
                    stageSelectButton = new Rectangle(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight);
                }
                continueButton = null;
            } else {
                // Easy: Retry and Stage Select
                retryButton = new Rectangle(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight);
                stageSelectButton = new Rectangle(centerX - buttonWidth / 2, startY - buttonHeight - buttonSpacing, buttonWidth, buttonHeight);
                continueButton = null;
            }
        }
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.begin();
        
        // Draw static background (same as Instructions screen)
        batch.draw(game.backgroundStatic, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(game.backgroundRectangle, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(game.shadow, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // Draw title
        game.titleFont.setColor(won ? Color.GOLD : Color.RED);
        String title = won ? "STAGE " + ROMAN_NUMERALS[stage - 1] + " COMPLETE!" : "STAGE " + ROMAN_NUMERALS[stage - 1] + " FAILED";
        layout.setText(game.titleFont, title);
        game.titleFont.draw(batch, title,
            (Gdx.graphics.getWidth() - layout.width) / 2,
            Gdx.graphics.getHeight() - 100);
        
        // Draw message
        game.bodyFont.setColor(Color.WHITE);
        String message;
        if (won) {
            if (stage < Core.MAX_STAGES) {
                message = "Stage " + ROMAN_NUMERALS[stage] + " unlocked!";
            } else {
                message = "All stages completed!";
            }
        } else {
            if (difficulty == 2) {
                message = "Hard difficulty: 1 life only. Progression reset on loss.";
            } else if (difficulty == 1) { // Medium difficulty
                int livesLeft = game.getMediumLives();
                if (livesLeft > 0) {
                    message = "Life consumed! Lives remaining: " + livesLeft + ". Try again or select another stage.";
                } else {
                    message = "No lives remaining. Medium difficulty progression reset.";
                }
            } else {
                message = "Try again or select another stage.";
            }
        }
        layout.setText(game.bodyFont, message);
        game.bodyFont.draw(batch, message,
            (Gdx.graphics.getWidth() - layout.width) / 2,
            Gdx.graphics.getHeight() - 180);
        
        // Get mouse position
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        
        // Draw buttons
        if (continueButton != null) {
            drawButton("Continue to Stage " + ROMAN_NUMERALS[stage], continueButton, mouseX, mouseY, true);
        }
        if (retryButton != null) {
            drawButton("Retry", retryButton, mouseX, mouseY, true);
        }
        if (stageSelectButton != null) {
            drawButton("Stage Select", stageSelectButton, mouseX, mouseY, true);
        }
        
        batch.end();
        
        // Handle clicks
        if (Gdx.input.justTouched()) {
            if (continueButton != null && continueButton.contains(mouseX, mouseY)) {
                game.playButtonSfx();
                    if (won && stage < Core.MAX_STAGES) {
                        game.completeStage(game.difficulty, stage);
                        game.setScreen(new GameScreen(game, stage + 1));
                    }
                return;
            }
            
            if (retryButton != null && retryButton.contains(mouseX, mouseY)) {
                game.playButtonSfx();
                game.setScreen(new GameScreen(game, stage));
                return;
            }
            
            if (stageSelectButton != null && stageSelectButton.contains(mouseX, mouseY)) {
                game.playButtonSfx();
                game.setScreen(new StageSelectScreen(game));
                return;
            }
        }
    }
    
    private void drawButton(String text, Rectangle bounds, float mouseX, float mouseY, boolean enabled) {
        boolean isHovered = bounds.contains(mouseX, mouseY) && enabled;
        
        if (enabled) {
            if (isHovered) {
                // Draw shadow for hovered button
                game.bodyFont.setColor(0, 0, 0, 0.3f);
                layout.setText(game.bodyFont, text);
                game.bodyFont.draw(batch, text,
                    bounds.x + (bounds.width - layout.width) / 2 + 2,
                    bounds.y + (bounds.height + layout.height) / 2 - 2);
                
                // Draw yellow text
                game.bodyFont.setColor(Color.YELLOW);
                game.bodyFont.draw(batch, text,
                    bounds.x + (bounds.width - layout.width) / 2,
                    bounds.y + (bounds.height + layout.height) / 2);
            } else {
                // Draw normal white text
                game.bodyFont.setColor(Color.WHITE);
                layout.setText(game.bodyFont, text);
                game.bodyFont.draw(batch, text,
                    bounds.x + (bounds.width - layout.width) / 2,
                    bounds.y + (bounds.height + layout.height) / 2);
            }
        } else {
            // Draw disabled (grayed out) text
            game.bodyFont.setColor(0.5f, 0.5f, 0.5f, 1f);
            layout.setText(game.bodyFont, text);
            game.bodyFont.draw(batch, text,
                bounds.x + (bounds.width - layout.width) / 2,
                bounds.y + (bounds.height + layout.height) / 2);
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
        batch.dispose();
    }
}