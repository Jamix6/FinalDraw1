package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class DifficultySelectScreen implements Screen {
    private final Core game;
    private SpriteBatch batch;
    private GlyphLayout layout;
    
    // Difficulty options
    private static final String[] DIFFICULTIES = {"Easy", "Medium", "Hard"};
    private static final String[] DESCRIPTIONS = {
        "For beginners - slower enemies, more health",
        "Standard challenge - balanced gameplay",
        "For experts - faster enemies, less health"
    };
    
    private Rectangle[] difficultyBounds;
    private Rectangle panelBounds;
    
    public DifficultySelectScreen(Core game) {
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
        
        // Panel centered
        float panelWidth = 500f;
        float panelHeight = 400f;
        panelBounds = new Rectangle(
            (screenWidth - panelWidth) / 2f,
            (screenHeight - panelHeight) / 2f,
            panelWidth, panelHeight
        );
        
        // Difficulty buttons (centered vertically in panel)
        difficultyBounds = new Rectangle[DIFFICULTIES.length];
        
        float buttonWidth = 300f;
        float buttonHeight = 60f;
        float buttonSpacing = 20f;
        float totalHeight = DIFFICULTIES.length * buttonHeight + (DIFFICULTIES.length - 1) * buttonSpacing;
        float startY = panelBounds.y + panelBounds.height - 100f;
        
        for (int i = 0; i < DIFFICULTIES.length; i++) {
            difficultyBounds[i] = new Rectangle(
                panelBounds.x + (panelBounds.width - buttonWidth) / 2f,
                startY - i * (buttonHeight + buttonSpacing),
                buttonWidth, buttonHeight
            );
        }
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.begin();
        
        // Draw static background with darker overlay
        batch.draw(game.backgroundStatic, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // Dark overlay for better readability
        batch.setColor(0f, 0f, 0f, 0.3f);
        batch.draw(game.backgroundStatic, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(Color.WHITE);
        batch.draw(game.shadow, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // Draw title
        game.titleFont.setColor(Color.WHITE);
        layout.setText(game.titleFont, "SELECT DIFFICULTY");
        game.titleFont.draw(batch, "SELECT DIFFICULTY",
            (Gdx.graphics.getWidth() - layout.width) / 2,
            Gdx.graphics.getHeight() - 50);
        
        // Draw panel background
        batch.setColor(0.15f, 0.15f, 0.15f, 0.95f);
        batch.draw(game.backgroundRectangle, panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height);
        batch.setColor(Color.WHITE);
        
        // Draw panel border
        batch.setColor(Color.YELLOW);
        batch.draw(game.backgroundRectangle, panelBounds.x, panelBounds.y, panelBounds.width, 2f);
        batch.draw(game.backgroundRectangle, panelBounds.x, panelBounds.y + panelBounds.height - 2f, panelBounds.width, 2f);
        batch.draw(game.backgroundRectangle, panelBounds.x, panelBounds.y, 2f, panelBounds.height);
        batch.draw(game.backgroundRectangle, panelBounds.x + panelBounds.width - 2f, panelBounds.y, 2f, panelBounds.height);
        batch.setColor(Color.WHITE);
        
        // Get mouse position
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        
        // Draw difficulty options
        for (int i = 0; i < DIFFICULTIES.length; i++) {
            Rectangle bounds = difficultyBounds[i];
            boolean isHovered = bounds.contains(mouseX, mouseY);
            
            // Draw button background
            if (isHovered) {
                batch.setColor(0.3f, 0.3f, 0.3f, 1f);
            } else {
                batch.setColor(0.2f, 0.2f, 0.2f, 1f);
            }
            batch.draw(game.backgroundRectangle, bounds.x, bounds.y, bounds.width, bounds.height);
            batch.setColor(Color.WHITE);
            
            // Draw button border
            if (isHovered) {
                batch.setColor(Color.YELLOW);
            } else {
                batch.setColor(Color.GRAY);
            }
            batch.draw(game.backgroundRectangle, bounds.x, bounds.y, bounds.width, 2f);
            batch.draw(game.backgroundRectangle, bounds.x, bounds.y + bounds.height - 2f, bounds.width, 2f);
            batch.draw(game.backgroundRectangle, bounds.x, bounds.y, 2f, bounds.height);
            batch.draw(game.backgroundRectangle, bounds.x + bounds.width - 2f, bounds.y, 2f, bounds.height);
            batch.setColor(Color.WHITE);
            
            // Draw difficulty name
            Color textColor;
            switch (i) {
                case 0: textColor = Color.GREEN; break; // Easy
                case 1: textColor = Color.YELLOW; break; // Medium
                case 2: textColor = Color.RED; break; // Hard
                default: textColor = Color.WHITE;
            }
            
            if (isHovered) {
                textColor = textColor.cpy().mul(1.2f); // Brighten on hover
            }
            
            game.titleFont.setColor(textColor);
            layout.setText(game.titleFont, DIFFICULTIES[i]);
            game.titleFont.draw(batch, DIFFICULTIES[i],
                bounds.x + (bounds.width - layout.width) / 2f,
                bounds.y + bounds.height - 20f);
            
            // Draw description
            game.bodyFont.setColor(Color.LIGHT_GRAY);
            layout.setText(game.bodyFont, DESCRIPTIONS[i]);
            game.bodyFont.draw(batch, DESCRIPTIONS[i],
                bounds.x + (bounds.width - layout.width) / 2f,
                bounds.y + 25f);
        }
        
        batch.end();
        
        // Handle clicks
        if (Gdx.input.justTouched()) {
            for (int i = 0; i < DIFFICULTIES.length; i++) {
                if (difficultyBounds[i].contains(mouseX, mouseY)) {
                    game.playButtonSfx();
                    game.difficulty = i; // 0=Easy, 1=Medium, 2=Hard
                    game.setScreen(new StageSelectScreen(game));
                    return;
                }
            }
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