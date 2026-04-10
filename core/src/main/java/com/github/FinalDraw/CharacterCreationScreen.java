package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class CharacterCreationScreen implements Screen {
    private final Core game;
    private final int slot;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private GlyphLayout layout;
    
    // UI elements
    private Rectangle panelBounds;
    private Rectangle nameInputBounds;
    private Rectangle createButtonBounds;
    private Rectangle cancelButtonBounds;
    
    // Name input
    private final StringBuilder nameBuilder;
    private boolean nameValid;
    private boolean nameInputFocused = true;
    private float cursorBlinkTimer;
    private static final float CURSOR_BLINK_INTERVAL = 0.5f;
    private boolean showCursor;

    // Default name suggestion
    private final String defaultName;

    // Theme colors
    private static final Color PANEL_BORDER = new Color(0.96f, 0.78f, 0.26f, 1f);
    private static final Color INPUT_BG = new Color(0.10f, 0.05f, 0.03f, 1f);
    private static final Color INPUT_BORDER_ACTIVE = new Color(0.96f, 0.78f, 0.26f, 1f);
    private static final Color INPUT_BORDER_INACTIVE = new Color(0.40f, 0.18f, 0.08f, 1f);

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
        shapeRenderer = new ShapeRenderer();
        layout = new GlyphLayout();
        
        updateButtonPositions();
        game.playMenuMusic();
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

    private void updateButtonPositions() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        // Smaller panel centered
        float panelWidth = 400f;
        float panelHeight = 250f;
        panelBounds = new Rectangle(
            (screenWidth - panelWidth) / 2f,
            (screenHeight - panelHeight) / 2f,
            panelWidth, panelHeight
        );
        
        // Name input field (centered in panel)
        float inputWidth = 300f;
        float inputHeight = 45f;
        nameInputBounds = new Rectangle(
            panelBounds.x + (panelBounds.width - inputWidth) / 2f,
            panelBounds.y + panelBounds.height / 2f - 10f,
            inputWidth, inputHeight
        );
        
        // Buttons at bottom of panel
        float buttonHeight = 40f;
        float buttonsWidth = 240f;
        float buttonsX = panelBounds.x + (panelBounds.width - buttonsWidth) / 2f;
        
        createButtonBounds = new Rectangle(
            buttonsX,
            panelBounds.y + 30f,
            buttonsWidth / 2f, buttonHeight
        );
        
        cancelButtonBounds = new Rectangle(
            buttonsX + buttonsWidth / 2f,
            panelBounds.y + 30f,
            buttonsWidth / 2f, buttonHeight
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
        // Background pattern (Background.png)
        batch.draw(game.backgroundStatic, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // Draw title
        game.titleFont.setColor(PANEL_BORDER);
        layout.setText(game.titleFont, "CREATE CHARACTER");
        game.titleFont.draw(batch, "CREATE CHARACTER",
            (Gdx.graphics.getWidth() - layout.width) / 2,
            Gdx.graphics.getHeight() - 50);
        batch.end();

        // ── Draw Rounded Panel and Input Rectangle using ShapeRenderer ───────
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        
        // Draw smaller rounded box (the panel)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.6f); // Semi-transparent dark
        drawRoundedRect(shapeRenderer, panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height, 20f);
        
        // Draw rectangle at the back of the text (input field background)
        shapeRenderer.setColor(INPUT_BG);
        shapeRenderer.rect(nameInputBounds.x, nameInputBounds.y, nameInputBounds.width, nameInputBounds.height);
        shapeRenderer.end();
        
        // Draw borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3.0f);
        
        // Panel border
        shapeRenderer.setColor(Color.BLACK);
        drawRoundedRectOutline(shapeRenderer, panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height, 20f);
        
        // Input field border
        Gdx.gl.glLineWidth(2.0f);
        shapeRenderer.setColor(nameInputFocused ? INPUT_BORDER_ACTIVE : INPUT_BORDER_INACTIVE);
        shapeRenderer.rect(nameInputBounds.x, nameInputBounds.y, nameInputBounds.width, nameInputBounds.height);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1.0f);
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        // Draw slot info
        game.bodyFont.setColor(Color.LIGHT_GRAY);
        String slotText = "Slot " + slot;
        layout.setText(game.bodyFont, slotText);
        game.bodyFont.draw(batch, slotText,
            panelBounds.x + (panelBounds.width - layout.width) / 2f,
            panelBounds.y + panelBounds.height - 25f);
        
        // Draw name text
        String name = nameBuilder.toString();
        game.bodyFont.setColor(Color.WHITE);
        layout.setText(game.bodyFont, name);
        
        // Center text inside the input field
        float textX = nameInputBounds.x + (nameInputBounds.width - layout.width) / 2f;
        float textY = nameInputBounds.y + (nameInputBounds.height + layout.height) / 2f;
        
        // If text is too long, align the end with the right edge of the input box
        float maxTextWidth = nameInputBounds.width - 20f;
        if (layout.width > maxTextWidth) {
            textX = nameInputBounds.x + 10f - (layout.width - maxTextWidth);
        }
        
        game.bodyFont.draw(batch, name, textX, textY);
        
        // Draw cursor if showing
        if (showCursor) {
            float cursorX = textX + layout.width;
            if (cursorX > nameInputBounds.x + nameInputBounds.width - 10f) {
                cursorX = nameInputBounds.x + nameInputBounds.width - 10f;
            }
            batch.setColor(Color.WHITE);
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
                message = "Name too short";
            } else if (name.length() > Core.MAX_NAME_LENGTH) {
                message = "Name too long";
            } else {
                message = "Invalid name";
            }
            
            game.bodyFont.setColor(Color.RED);
            layout.setText(game.bodyFont, message);
            game.bodyFont.draw(batch, message,
                panelBounds.x + (panelBounds.width - layout.width) / 2f,
                nameInputBounds.y - 20f);
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
            
            // Focus name input field if clicked, otherwise unfocus it
            if (nameInputBounds.contains(mouseX, mouseY)) {
                nameInputFocused = true;
                cursorBlinkTimer = 0f;
                showCursor = true;
            } else {
                nameInputFocused = false;
            }
        }
    }
    
    private void handleKeyboardInput() {
        if (!nameInputFocused) {
            return;
        }

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

        // Handle basic printable keys
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            appendCharacter(' ');
        }

        boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

        appendKey(Input.Keys.A, 'a', 'A', shift);
        appendKey(Input.Keys.B, 'b', 'B', shift);
        appendKey(Input.Keys.C, 'c', 'C', shift);
        appendKey(Input.Keys.D, 'd', 'D', shift);
        appendKey(Input.Keys.E, 'e', 'E', shift);
        appendKey(Input.Keys.F, 'f', 'F', shift);
        appendKey(Input.Keys.G, 'g', 'G', shift);
        appendKey(Input.Keys.H, 'h', 'H', shift);
        appendKey(Input.Keys.I, 'i', 'I', shift);
        appendKey(Input.Keys.J, 'j', 'J', shift);
        appendKey(Input.Keys.K, 'k', 'K', shift);
        appendKey(Input.Keys.L, 'l', 'L', shift);
        appendKey(Input.Keys.M, 'm', 'M', shift);
        appendKey(Input.Keys.N, 'n', 'N', shift);
        appendKey(Input.Keys.O, 'o', 'O', shift);
        appendKey(Input.Keys.P, 'p', 'P', shift);
        appendKey(Input.Keys.Q, 'q', 'Q', shift);
        appendKey(Input.Keys.R, 'r', 'R', shift);
        appendKey(Input.Keys.S, 's', 'S', shift);
        appendKey(Input.Keys.T, 't', 'T', shift);
        appendKey(Input.Keys.U, 'u', 'U', shift);
        appendKey(Input.Keys.V, 'v', 'V', shift);
        appendKey(Input.Keys.W, 'w', 'W', shift);
        appendKey(Input.Keys.X, 'x', 'X', shift);
        appendKey(Input.Keys.Y, 'y', 'Y', shift);
        appendKey(Input.Keys.Z, 'z', 'Z', shift);

        appendKey(Input.Keys.NUM_0, '0', ')', shift);
        appendKey(Input.Keys.NUM_1, '1', '!', shift);
        appendKey(Input.Keys.NUM_2, '2', '@', shift);
        appendKey(Input.Keys.NUM_3, '3', '#', shift);
        appendKey(Input.Keys.NUM_4, '4', '$', shift);
        appendKey(Input.Keys.NUM_5, '5', '%', shift);
        appendKey(Input.Keys.NUM_6, '6', '^', shift);
        appendKey(Input.Keys.NUM_7, '7', '&', shift);
        appendKey(Input.Keys.NUM_8, '8', '*', shift);
        appendKey(Input.Keys.NUM_9, '9', '(', shift);

        appendKey(Input.Keys.MINUS, '-', '_', shift);
        appendKey(Input.Keys.EQUALS, '=', '+', shift);
        appendKey(Input.Keys.COMMA, ',', '<', shift);
        appendKey(Input.Keys.PERIOD, '.', '>', shift);
        appendKey(Input.Keys.SLASH, '/', '?', shift);
        appendKey(Input.Keys.SEMICOLON, ';', ':', shift);
        appendKey(Input.Keys.APOSTROPHE, '\'', '"', shift);
        appendKey(Input.Keys.LEFT_BRACKET, '[', '{', shift);
        appendKey(Input.Keys.RIGHT_BRACKET, ']', '}', shift);
        appendKey(Input.Keys.BACKSLASH, '\\', '|', shift);

        appendKey(Input.Keys.GRAVE, '`', '~', shift);
    }

    private void appendKey(int key, char normal, char shifted, boolean shift) {
        if (Gdx.input.isKeyJustPressed(key)) {
            appendCharacter(shift ? shifted : normal);
        }
    }

    private void appendCharacter(char c) {
        if (nameBuilder.length() >= Core.MAX_NAME_LENGTH) {
            return;
        }
        if (c == '\u0011') {
            return;
        }
        nameBuilder.append(c);
        validateName();
        cursorBlinkTimer = 0f;
        showCursor = true;
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
        // Draw button text only for a minimalistic design
        if (!enabled) {
            game.bodyFont.setColor(0.5f, 0.5f, 0.5f, 1f);
        } else if (isHovered) {
            game.bodyFont.setColor(PANEL_BORDER);
        } else {
            game.bodyFont.setColor(Color.WHITE);
        }
        layout.setText(game.bodyFont, text);
        float textX = bounds.x + (bounds.width - layout.width) / 2f;
        float textY = bounds.y + (bounds.height + layout.height) / 2f;
        game.bodyFont.draw(batch, text, textX, textY);
        
        if (isHovered && enabled) {
            batch.setColor(PANEL_BORDER);
            batch.draw(game.backgroundRectangle, textX, bounds.y + 8f, layout.width, 1.5f);
            batch.setColor(Color.WHITE);
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
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}