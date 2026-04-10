package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class StageCompleteScreen implements Screen {
    private final Core game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private GlyphLayout layout;

    private int stage;
    private boolean won;
    private int difficulty;

    // UI Panel
    private Rectangle panelBounds;

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
        shapeRenderer = new ShapeRenderer();
        layout = new GlyphLayout();

        updateButtonPositions();
        game.playMenuMusic();
        
        if (won) {
            game.playWinSfx();
        } else {
            game.playLoseSfx();
        }
    }

    private void updateButtonPositions() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        float centerX = screenWidth / 2f;
        float buttonWidth = 300f;
        float buttonHeight = 50f;
        float buttonSpacing = 15f;

        // Position buttons closer to the message (which is at H - 180)
        float startY = screenHeight - 260f; 

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

        // Define panel bounds to wrap message and buttons
        float panelW = 450f;
        float panelH = 290f;
        panelBounds = new Rectangle(centerX - panelW / 2f, screenHeight - 140f - panelH, panelW, panelH);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Draw static background & shadow
        batch.draw(game.backgroundStatic, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(game.shadow, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Draw title
        game.titleFont.setColor(won ? Color.GOLD : Color.RED);
        String title = won ? "STAGE " + ROMAN_NUMERALS[stage - 1] + " COMPLETE!" : "STAGE " + ROMAN_NUMERALS[stage - 1] + " FAILED";
        layout.setText(game.titleFont, title);
        game.titleFont.draw(batch, title,
            (Gdx.graphics.getWidth() - layout.width) / 2,
            Gdx.graphics.getHeight() - 50);
        batch.end();

        // Draw black box background for content
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        
        // Rounded black box
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        drawRoundedRect(shapeRenderer, panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height, 20f);
        shapeRenderer.end();

        // Thick black outline
        Gdx.gl.glLineWidth(3.0f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        drawRoundedRectOutline(shapeRenderer, panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height, 20f);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1.0f);
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        // Draw message in Light Blue
        game.bodyFont.setColor(Color.CYAN);
        String message;
        if (won) {
            if (stage < Core.MAX_STAGES) {
                message = "Stage " + ROMAN_NUMERALS[stage] + " unlocked!";
            } else {
                message = "All stages completed!";
            }
        } else {
            game.bodyFont.setColor(Color.WHITE); // Keep failure messages white or red
            if (difficulty == 2) {
                message = "Hard: 1 life. Reset on loss";
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
            String buttonText = stage < Core.MAX_STAGES ? "Continue to Stage " + ROMAN_NUMERALS[stage] : "Back to Menu";
            drawButton(buttonText, continueButton, mouseX, mouseY, true);
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
                if (won) {
                    if (stage >= Core.MAX_STAGES) {
                        game.setScreen(new MenuScreen(game));
                    } else {
                        game.completeStage(game.difficulty, stage);
                        game.setScreen(new GameScreen(game, stage + 1));
                    }
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
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
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
}
