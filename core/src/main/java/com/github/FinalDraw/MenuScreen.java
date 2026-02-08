package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Rectangle;

public class MenuScreen implements Screen {
    private final Core game;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture logoTexture;
    private GlyphLayout layout;

    // Menu items
    private static final String[] MENU_ITEMS = {"Play", "Instructions", "Other", "Exit"};
    private Rectangle[] menuBounds;

    private static final float LEFT_MARGIN = 50;
    private static final float TEXT_SPACING = 60;

    public MenuScreen(Core game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        layout = new GlyphLayout();

        // Load font
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/AgencyFB.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 36;
        parameter.color = Color.WHITE;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        font = generator.generateFont(parameter);
        generator.dispose();

        // Load logo
        logoTexture = new Texture(Gdx.files.internal("logo(white).png"));

        // Create menu bounds
        menuBounds = new Rectangle[MENU_ITEMS.length];
        updateMenuPositions();
    }

    private void updateMenuPositions() {
        float startY = (Gdx.graphics.getHeight() + TEXT_SPACING * (MENU_ITEMS.length - 1)) / 2;

        for (int i = 0; i < MENU_ITEMS.length; i++) {
            layout.setText(font, MENU_ITEMS[i]);
            float y = startY - (i * TEXT_SPACING);
            menuBounds[i] = new Rectangle(LEFT_MARGIN, y - layout.height, layout.width, layout.height);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Draw logo
        float logoSize = 300;
        batch.draw(logoTexture, 20, Gdx.graphics.getHeight() - logoSize + 20, logoSize, logoSize);

        // Draw menu items
        for (int i = 0; i < MENU_ITEMS.length; i++) {
            font.draw(batch, MENU_ITEMS[i], LEFT_MARGIN, menuBounds[i].y + menuBounds[i].height);
        }

        batch.end();

        // Handle clicks
        if (Gdx.input.justTouched()) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

            for (int i = 0; i < MENU_ITEMS.length; i++) {
                if (menuBounds[i].contains(mouseX, mouseY)) {
                    handleMenuClick(i);
                    break;
                }
            }
        }
    }

    private void handleMenuClick(int index) {
        switch (index) {
            case 0: System.out.println("Play wala pa"); break;
            case 1: System.out.println("Instructions"); break;
            case 2: System.out.println("Test Click #3 rar"); break;
            case 3: Gdx.app.exit(); break;
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width > 0 && height > 0) {
            updateMenuPositions();
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
        font.dispose();
        logoTexture.dispose();
    }
}
