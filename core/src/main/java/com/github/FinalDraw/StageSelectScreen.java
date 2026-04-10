package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class StageSelectScreen implements Screen {
    private final Core game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private GlyphLayout layout;

    // Background panel for stages
    private Rectangle stagePanelBounds;

    // Maroon color for unlocked tiles (clear/transparent)
    private static final Color MAROON = new Color(0, 0, 0, 0);
    // Gray color for locked tiles (90% opacity)
    private static final Color GRAY_90 = new Color(0.5f, 0.5f, 0.5f, 0.9f);

    // Stage buttons
    private static final int MAX_STAGES = Core.MAX_STAGES;
    private Array<Rectangle> stageBounds;
    private Rectangle backButton;
    private int hoveredStage = -1;
    private int unlockedStage;

    // Stage preview
    private String previewText;
    private Rectangle previewBounds;
    private boolean showPreview;

    // Roman numerals for stages
    private static final String[] ROMAN_NUMERALS = {"I", "II", "III", "IV", "V"};

    public StageSelectScreen(Core game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        layout = new GlyphLayout();

        unlockedStage = game.getUnlockedStage(game.difficulty);
        updateButtonPositions();

        game.playMenuMusic();
    }

    private void updateButtonPositions() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Stage buttons in a grid: 2 rows, 3 columns
        stageBounds = new Array<>(MAX_STAGES);
        float buttonSize = 120f;
        float buttonSpacing = 40f;
        float gridWidth = 3 * buttonSize + 2 * buttonSpacing;
        float gridHeight = 2 * buttonSize + buttonSpacing;
        float gridX = (screenWidth - gridWidth) / 2f;
        float gridY = (screenHeight - gridHeight) / 2f + 50f;

        for (int i = 0; i < MAX_STAGES; i++) {
            int row = i / 3;
            int col = i % 3;
            float x = gridX + col * (buttonSize + buttonSpacing);
            float y = gridY + (1 - row) * (buttonSize + buttonSpacing); // Top to bottom
            stageBounds.add(new Rectangle(x, y, buttonSize, buttonSize));
        }

        // Add padding for the stage panel background box
        float panelPadding = 40f;
        stagePanelBounds = new Rectangle(
            gridX - panelPadding,
            gridY - panelPadding,
            gridWidth + 2 * panelPadding,
            gridHeight + 2 * panelPadding
        );

        // Back button at bottom center
        backButton = new Rectangle(screenWidth/2 - 75, 80, 150, 50);

        // Preview area at top (adjusted to avoid overlapping with title)
        previewBounds = new Rectangle(50, screenHeight - 180, screenWidth - 100, 100);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Get mouse position
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        // Update hovered stage
        hoveredStage = -1;
        for (int i = 0; i < stageBounds.size; i++) {
            if (stageBounds.get(i).contains(mouseX, mouseY)) {
                hoveredStage = i;
                break;
            }
        }

        batch.begin();
        // Draw static background (Background.png)
        batch.draw(game.backgroundStatic, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Draw title (moved higher to make space for stage preview)
        game.titleFont.setColor(Color.WHITE);
        layout.setText(game.titleFont, "SELECT STAGE");
        game.titleFont.draw(batch, "SELECT STAGE",
            (Gdx.graphics.getWidth() - layout.width) / 2,
            Gdx.graphics.getHeight() - 25);
        batch.end();

        // Draw background panel for levels
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        
        // Rounded black background (similar to audio box)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f); 
        drawRoundedRect(shapeRenderer, stagePanelBounds.x, stagePanelBounds.y, stagePanelBounds.width, stagePanelBounds.height, 20f);
        shapeRenderer.end();

        // Thick black outline for the panel
        Gdx.gl.glLineWidth(3.0f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        drawRoundedRectOutline(shapeRenderer, stagePanelBounds.x, stagePanelBounds.y, stagePanelBounds.width, stagePanelBounds.height, 20f);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1.0f);

        // Draw tile backgrounds using ShapeRenderer
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < stageBounds.size; i++) {
            Rectangle bounds = stageBounds.get(i);
            boolean isUnlocked = (i + 1) <= unlockedStage;
            
            if (!isUnlocked) {
                shapeRenderer.setColor(GRAY_90);
            } else {
                shapeRenderer.setColor(MAROON);
            }
            drawRoundedRect(shapeRenderer, bounds.x, bounds.y, bounds.width, bounds.height, 15f);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2.0f);
        for (int i = 0; i < stageBounds.size; i++) {
            Rectangle bounds = stageBounds.get(i);
            boolean isUnlocked = (i + 1) <= unlockedStage;
            boolean isHovered = (i == hoveredStage);
            
            if (!isUnlocked) {
                shapeRenderer.setColor(Color.DARK_GRAY);
            } else if (isHovered) {
                shapeRenderer.setColor(Color.YELLOW);
            } else {
                shapeRenderer.setColor(Color.WHITE);
            }
            drawRoundedRectOutline(shapeRenderer, bounds.x, bounds.y, bounds.width, bounds.height, 15f);
        }
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1.0f);
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        // Draw stage button text
        for (int i = 0; i < stageBounds.size; i++) {
            Rectangle bounds = stageBounds.get(i);
            boolean isUnlocked = (i + 1) <= unlockedStage;
            boolean isHovered = (i == hoveredStage);

            drawStageButtonText(bounds, i + 1, isUnlocked, isHovered);
        }

        // Draw back button
        drawButton("Back to Menu", backButton, mouseX, mouseY, true);

        // Draw stage preview if hovering
        if (hoveredStage >= 0 && hoveredStage < MAX_STAGES) {
            int stageNumber = hoveredStage + 1;
            boolean isUnlocked = stageNumber <= unlockedStage;

            game.bodyFont.setColor(isUnlocked ? Color.CYAN : Color.LIGHT_GRAY);
            String preview = getStagePreview(stageNumber, isUnlocked);
            layout.setText(game.bodyFont, preview);

            float previewX = previewBounds.x + (previewBounds.width - layout.width) / 2;
            float previewY = previewBounds.y + previewBounds.height - 5;
            game.bodyFont.draw(batch, preview, previewX, previewY);
        }

        // Draw lives for Medium or Hard difficulty
        if (game.difficulty == 1) { // Medium difficulty
            game.bodyFont.setColor(Color.YELLOW);
            String livesText = "Lives: " + game.getMediumLives();
            layout.setText(game.bodyFont, livesText);
            game.bodyFont.draw(batch, livesText, 50, 50);
        } else if (game.difficulty == 2) { // Hard difficulty
            game.bodyFont.setColor(Color.ORANGE);
            String livesText = "Lives: 1";
            layout.setText(game.bodyFont, livesText);
            game.bodyFont.draw(batch, livesText, 50, 50);
        }

        batch.end();

        // Handle clicks
        if (Gdx.input.justTouched()) {
            if (backButton.contains(mouseX, mouseY)) {
                game.playButtonSfx();
                game.setScreen(new MenuScreen(game));
                return;
            }

            for (int i = 0; i < stageBounds.size; i++) {
                if (stageBounds.get(i).contains(mouseX, mouseY)) {
                    int stageNumber = i + 1;
                    if (stageNumber <= unlockedStage) {
                        game.playButtonSfx();
                        // Store selected stage and show difficulty selection
                        // We'll use a temporary variable or modify MenuScreen
                        // For now, go to GameScreen with default difficulty
                        game.setScreen(new GameScreen(game, stageNumber));
                        return;
                    }
                }
            }
        }
    }

    private void drawStageButtonText(Rectangle bounds, int stageNumber, boolean isUnlocked, boolean isHovered) {
        Color textColor;

        if (!isUnlocked) {
            textColor = Color.DARK_GRAY;
        } else if (isHovered) {
            textColor = Color.YELLOW;
        } else {
            // Unlocked but not hovered: use White
            textColor = Color.WHITE;
        }

        // Draw Roman numeral
        String romanNumeral = ROMAN_NUMERALS[stageNumber - 1];
        layout.setText(game.bodyFont, romanNumeral);
        game.bodyFont.setColor(textColor);
        game.bodyFont.draw(batch, romanNumeral,
            bounds.x + (bounds.width - layout.width) / 2,
            bounds.y + (bounds.height + layout.height) / 2);

        // Draw lock icon for locked stages
        if (!isUnlocked) {
            game.bodyFont.setColor(Color.DARK_GRAY);
            game.bodyFont.draw(batch, "LOCKED",
                bounds.x + (bounds.width - 60) / 2,
                bounds.y + 30);
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

    private String getStagePreview(int stageNumber, boolean isUnlocked) {
        String base = "STAGE " + ROMAN_NUMERALS[stageNumber - 1];

        if (!isUnlocked) {
            return base + " - LOCKED";
        }

        StageConfig config = StageConfig.forStage(stageNumber);
        if (config != null) {
            return base + " - " + config.description;
        }
        switch (stageNumber) {
            case 1:
                return base + " - Tutorial: Learn the basics";
            case 2:
                return base + " - Introduction: Standard gameplay";
            case 3:
                return base + " - Challenge: Increased difficulty";
            case 4:
                return base + " - Expert: Advanced mechanics";
            case 5:
                return base + " - Final: Ultimate test";
            default:
                return base;
        }
    }

    private void drawRoundedRect(ShapeRenderer renderer, float x, float y, float width, float height, float radius) {
        // Draw the filled rounded rectangle using arcs and rects
        // Center horizontal
        renderer.rect(x + radius, y, width - 2 * radius, height);
        // Left vertical
        renderer.rect(x, y + radius, radius, height - 2 * radius);
        // Right vertical
        renderer.rect(x + width - radius, y + radius, radius, height - 2 * radius);
        // Corners
        renderer.arc(x + radius, y + radius, radius, 180, 90);
        renderer.arc(x + width - radius, y + radius, radius, 270, 90);
        renderer.arc(x + width - radius, y + height - radius, radius, 0, 90);
        renderer.arc(x + radius, y + height - radius, radius, 90, 90);
    }

    private void drawRoundedRectOutline(ShapeRenderer renderer, float x, float y, float width, float height, float radius) {
        // Draw only the outer edges
        // Bottom
        renderer.line(x + radius, y, x + width - radius, y);
        // Top
        renderer.line(x + radius, y + height, x + width - radius, y + height);
        // Left
        renderer.line(x, y + radius, x, y + height - radius);
        // Right
        renderer.line(x + width, y + radius, x + width, y + height - radius);
        
        // Corners - we need to draw arcs without the center lines
        // ShapeRenderer.arc(x, y, radius, start, degrees) draws lines to the center.
        // We can use a custom function or many small lines to draw the corner arc only.
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
        batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
