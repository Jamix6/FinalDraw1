package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class StageSelectScreen implements Screen {
    private final Core game;
    private SpriteBatch batch;
    private GlyphLayout layout;
    
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
        
        // Back button at bottom center
        backButton = new Rectangle(screenWidth/2 - 75, 80, 150, 50);
        
        // Preview area at top (adjusted to avoid overlapping with title)
        previewBounds = new Rectangle(50, screenHeight - 180, screenWidth - 100, 100);
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
        
        // Draw title (moved higher to make space for stage preview)
        game.titleFont.setColor(Color.WHITE);
        layout.setText(game.titleFont, "SELECT STAGE");
        game.titleFont.draw(batch, "SELECT STAGE",
            (Gdx.graphics.getWidth() - layout.width) / 2,
            Gdx.graphics.getHeight() - 25);
        
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
        
        // Draw stage buttons
        for (int i = 0; i < stageBounds.size; i++) {
            Rectangle bounds = stageBounds.get(i);
            boolean isUnlocked = (i + 1) <= unlockedStage;
            boolean isHovered = (i == hoveredStage);
            
            drawStageButton(bounds, i + 1, isUnlocked, isHovered);
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
            float previewY = previewBounds.y + previewBounds.height - 20;
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
    
    private void drawStageButton(Rectangle bounds, int stageNumber, boolean isUnlocked, boolean isHovered) {
        Color borderColor;
        Color textColor;
        
        if (!isUnlocked) {
            borderColor = Color.GRAY;
            textColor = Color.DARK_GRAY;
        } else if (isHovered) {
            borderColor = Color.YELLOW;
            textColor = Color.YELLOW;
        } else {
            borderColor = Color.WHITE;
            textColor = Color.WHITE;
        }
        
        // Draw button background
        game.bodyFont.setColor(borderColor);
        String romanNumeral = ROMAN_NUMERALS[stageNumber - 1];
        layout.setText(game.bodyFont, romanNumeral);
        
        // Draw Roman numeral
        game.bodyFont.setColor(textColor);
        game.bodyFont.draw(batch, romanNumeral,
            bounds.x + (bounds.width - layout.width) / 2,
            bounds.y + (bounds.height + layout.height) / 2);
        
        // Draw lock icon for locked stages
        if (!isUnlocked) {
            game.bodyFont.setColor(Color.GRAY);
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
        
        // Stage-specific previews (will be expanded with difficulty mechanics later)
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