package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class CharacterCreationScreen implements Screen {
    private final Core game;
    private final int slot;
    private SpriteBatch batch;
    private GlyphLayout layout;
    
    // UI elements
    private Rectangle panelBounds;
    private Rectangle nameInputBounds;
    private Rectangle createButtonBounds;
    private Rectangle cancelButtonBounds;
    
    // Name input
    private StringBuilder nameBuilder;
    private boolean nameValid;
    private float cursorBlinkTimer;
    private static final float CURSOR_BLINK_INTERVAL = 0.5f;
    private boolean showCursor;
    
    // Default name suggestion
    private String defaultName;
    
    public CharacterCreationScreen(Core game, int slot) {
        this.game = game;
        this.slot = slot;
        this.nameBuilder = new StringBuilder();
        this.defaultName = "User" + slot;
        this.nameBuilder.append(defaultName);
        validateName();
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
        float panelHeight = 300f;
        panelBounds = new Rectangle(
            (screenWidth - panelWidth) / 2f,
            (screenHeight - panelHeight) / 2f,
            panelWidth, panelHeight
        );
        
        // Name input field (centered in panel)
        float inputWidth = 300f;
        float inputHeight = 40f;
        nameInputBounds = new Rectangle(
            panelBounds.x + (panelBounds.width - inputWidth) / 2f,
            panelBounds.y + panelBounds.height - 100f,
            inputWidth, inputHeight
        );
        
        // Buttons at bottom of panel
        float buttonWidth = 120f;
        float buttonHeight = 40f;
        float buttonSpacing = 30f;
        float totalButtonsWidth = 2 * buttonWidth + buttonSpacing;
        float buttonsX = panelBounds.x + (panelBounds.width - totalButtonsWidth) / 2f;
        
        createButtonBounds = new Rectangle(
            buttonsX,
            panelBounds.y + 50f,
            buttonWidth, buttonHeight
        );
        
        cancelButtonBounds = new Rectangle(
            buttonsX + buttonWidth + buttonSpacing,
            panelBounds.y + 50f,
            buttonWidth, buttonHeight
        );
    }
    
    private void validateName() {
        String name = nameBuilder.toString().trim();
        nameValid = name.length() >= Core.MIN_NAME_LENGTH && 
                   name.length() <= Core.MAX_NAME_LENGTH &&
                   !name.isEmpty();
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Update cursor blink
        cursorBlinkTimer += delta;
        if (cursorBlinkTimer >= CURSOR_BLINK_INTERVAL) {
            cursorBlinkTimer = 0f;
            showCursor = !showCursor;
        }
        
        // Handle keyboard input for name
        handleKeyboardInput();
        
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
        layout.setText(game.titleFont, "CREATE CHARACTER");
        game.titleFont.draw(batch, "CREATE CHARACTER",
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
        
        // Draw slot info
        game.bodyFont.setColor(Color.LIGHT_GRAY);
        String slotText = "Slot " + slot;
        layout.setText(game.bodyFont, slotText);
        game.bodyFont.draw(batch, slotText,
            panelBounds.x + (panelBounds.width - layout.width) / 2f,
            panelBounds.y + panelBounds.height - 40f);
        
        // Draw name label
        game.bodyFont.setColor(Color.WHITE);
        String nameLabel = "Character Name:";
        layout.setText(game.bodyFont, nameLabel);
        game.bodyFont.draw(batch, nameLabel,
            panelBounds.x + (panelBounds.width - layout.width) / 2f,
            panelBounds.y + panelBounds.height - 80f);
        
        // Draw name input field background
        batch.setColor(0.1f, 0.1f, 0.1f, 1f);
        batch.draw(game.backgroundRectangle, nameInputBounds.x, nameInputBounds.y, nameInputBounds.width, nameInputBounds.height);
        batch.setColor(Color.WHITE);
        
        // Draw name input field border
        batch.setColor(nameValid ? Color.GREEN : Color.RED);
        batch.draw(game.backgroundRectangle, nameInputBounds.x, nameInputBounds.y, nameInputBounds.width, 2f);
        batch.draw(game.backgroundRectangle, nameInputBounds.x, nameInputBounds.y + nameInputBounds.height - 2f, nameInputBounds.width, 2f);
        batch.draw(game.backgroundRectangle, nameInputBounds.x, nameInputBounds.y, 2f, nameInputBounds.height);
        batch.draw(game.backgroundRectangle, nameInputBounds.x + nameInputBounds.width - 2f, nameInputBounds.y, 2f, nameInputBounds.height);
        batch.setColor(Color.WHITE);
        
        // Draw name text
        String name = nameBuilder.toString();
        game.bodyFont.setColor(Color.WHITE);
        layout.setText(game.bodyFont, name);
        
        // Calculate text position (centered in input field)
        float textX = nameInputBounds.x + 10f;
        float textY = nameInputBounds.y + (nameInputBounds.height + layout.height) / 2f;
        
        // If text is too long, show it with offset
        float maxTextWidth = nameInputBounds.width - 20f;
        if (layout.width > maxTextWidth) {
            // Text is too long, we need to show the end of it
            float overflow = layout.width - maxTextWidth;
            textX -= overflow;
        }
        
        game.bodyFont.draw(batch, name, textX, textY);
        
        // Draw cursor if showing
        if (showCursor) {
            float cursorX = textX + layout.width;
            if (cursorX > nameInputBounds.x + nameInputBounds.width - 10f) {
                cursorX = nameInputBounds.x + nameInputBounds.width - 10f;
            }
            batch.setColor(Color.YELLOW);
            batch.draw(game.backgroundRectangle, cursorX, textY - layout.height, 2f, layout.height);
            batch.setColor(Color.WHITE);
        }
        
        // Draw character count
        String countText = name.length() + "/" + Core.MAX_NAME_LENGTH;
        game.bodyFont.setColor(name.length() >= Core.MIN_NAME_LENGTH ? Color.LIGHT_GRAY : Color.RED);
        layout.setText(game.bodyFont, countText);
        game.bodyFont.draw(batch, countText,
            nameInputBounds.x + nameInputBounds.width - layout.width - 5f,
            nameInputBounds.y - 5f);
        
        // Draw validation message
        if (!nameValid) {
            String message;
            if (name.length() < Core.MIN_NAME_LENGTH) {
                message = "Name must be at least " + Core.MIN_NAME_LENGTH + " character";
                if (Core.MIN_NAME_LENGTH > 1) message += "s";
            } else if (name.length() > Core.MAX_NAME_LENGTH) {
                message = "Name cannot exceed " + Core.MAX_NAME_LENGTH + " characters";
            } else {
                message = "Invalid name";
            }
            
            game.bodyFont.setColor(Color.RED);
            layout.setText(game.bodyFont, message);
            game.bodyFont.draw(batch, message,
                panelBounds.x + (panelBounds.width - layout.width) / 2f,
                nameInputBounds.y - 25f);
        }
        
        // Get mouse position
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        
        // Draw buttons
        boolean createHovered = createButtonBounds.contains(mouseX, mouseY);
        boolean cancelHovered = cancelButtonBounds.contains(mouseX, mouseY);
        
        drawButton("Create", createButtonBounds, createHovered, nameValid);
        drawButton("Cancel", cancelButtonBounds, cancelHovered, true);
        
        batch.end();
        
        // Handle clicks
        if (Gdx.input.justTouched()) {
            if (createButtonBounds.contains(mouseX, mouseY) && nameValid) {
                game.playButtonSfx();
                createCharacter();
                return;
            }
            
            if (cancelButtonBounds.contains(mouseX, mouseY)) {
                game.playButtonSfx();
                game.setScreen(new CharacterSelectScreen(game));
                return;
            }
            
            // Focus name input field if clicked
            if (nameInputBounds.contains(mouseX, mouseY)) {
                // Already focused, but we can reset cursor blink
                cursorBlinkTimer = 0f;
                showCursor = true;
            }
        }
    }
    
    private void handleKeyboardInput() {
        // Handle backspace
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            if (nameBuilder.length() > 0) {
                nameBuilder.deleteCharAt(nameBuilder.length() - 1);
                validateName();
                cursorBlinkTimer = 0f;
                showCursor = true;
            }
        }
        
        // Handle enter key (create character if valid)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && nameValid) {
            createCharacter();
            return;
        }
        
        // Handle escape key (cancel)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new CharacterSelectScreen(game));
            return;
        }
        
        // Handle character input - we'll use a simpler approach
        // Since getTextInput() requires a listener, we'll handle individual key presses
        // This is already handled by the text input system in LibGDX
        // We'll rely on the system's text input handling
    }
    
    private void createCharacter() {
        String name = nameBuilder.toString().trim();
        if (name.length() < Core.MIN_NAME_LENGTH || name.length() > Core.MAX_NAME_LENGTH) {
            return;
        }
        
        Core.SaveProfile profile = game.createProfile(slot, name);
        if (profile != null) {
            game.setCurrentProfileSlot(slot);
            game.setScreen(new MenuScreen(game));
        }
    }
    
    private void drawButton(String text, Rectangle bounds, boolean isHovered, boolean enabled) {
        // Draw button background
        if (!enabled) {
            batch.setColor(0.2f, 0.2f, 0.2f, 1f);
        } else if (isHovered) {
            batch.setColor(0.3f, 0.3f, 0.3f, 1f);
        } else {
            batch.setColor(0.25f, 0.25f, 0.25f, 1f);
        }
        batch.draw(game.backgroundRectangle, bounds.x, bounds.y, bounds.width, bounds.height);
        batch.setColor(Color.WHITE);
        
        // Draw button border
        if (!enabled) {
            batch.setColor(Color.DARK_GRAY);
        } else if (isHovered) {
            batch.setColor(Color.YELLOW);
        } else {
            batch.setColor(Color.GRAY);
        }
        batch.draw(game.backgroundRectangle, bounds.x, bounds.y, bounds.width, 2f);
        batch.draw(game.backgroundRectangle, bounds.x, bounds.y + bounds.height - 2f, bounds.width, 2f);
        batch.draw(game.backgroundRectangle, bounds.x, bounds.y, 2f, bounds.height);
        batch.draw(game.backgroundRectangle, bounds.x + bounds.width - 2f, bounds.y, 2f, bounds.height);
        batch.setColor(Color.WHITE);
        
        // Draw button text
        if (!enabled) {
            game.bodyFont.setColor(0.5f, 0.5f, 0.5f, 1f);
        } else if (isHovered) {
            game.bodyFont.setColor(Color.YELLOW);
        } else {
            game.bodyFont.setColor(Color.WHITE);
        }
        layout.setText(game.bodyFont, text);
        game.bodyFont.draw(batch, text,
            bounds.x + (bounds.width - layout.width) / 2f,
            bounds.y + (bounds.height + layout.height) / 2f);
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