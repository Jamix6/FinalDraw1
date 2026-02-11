package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;

public class Instructions implements Screen {
    private final Core game;
    private SpriteBatch batch;
    private GlyphLayout layout;

    // Navigation
    private int currentPage = 0;
    private static final int TOTAL_PAGES = 5;
    private Rectangle prevButton, nextButton, backButton;

    // Page content
    private static final String[] PAGE_TITLES = {
        "[How to Win?]",
        "[Cards]",
        "[Game Progression]",
        "[Powers]",
        "[Risk]"
    };

    private static final String[][] PAGE_CONTENT = {
        {
            "- The player who scores closer to 21 wins.",
            "",
            "- If both players have a score greater than 21,",
            "the player whose score is closer to 21 wins.",
            "",
            "- If both players have the same score, it's a draw."
        },
        {
            "- The cards in the deck are numbered from 1 to 11,",
            "all cards are unique so there are no repetitions.",
            "",
            "- It is impossible to draw the same card that is",
            "already placed on the table."
        },
        {
            "- Player takes 2 random cards, the first card is",
            "turned over and the opponent cannot see the",
            "number on it.",
            "",
            "- The player draws a card or passes on his turn.",
            "The game continues until both players pass.",
            "",
            "- After both players pass, they turn over the",
            "cards to determine the winner."
        },
        {
            "Work in Progress."
        },
        {
            "- The distance of your demise is determined by",
            "the RISK. The risk changes every round.",
            "",
            "The risk can be seen on... [wip]",
            "",
            "Once your health reaches the risk, you will lose.",
            "",
            "Enjoy :)"
        }
    };

    public Instructions(Core game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        layout = new GlyphLayout();

        updateButtonPositions();
    }

    private void updateButtonPositions() {
        float screenWidth = Gdx.graphics.getWidth();
        float buttonY = 80;
        float buttonHeight = 50;

        // Previous button (left)
        prevButton = new Rectangle(50, buttonY, 120, buttonHeight);

        // Back to Menu button (center)
        backButton = new Rectangle(screenWidth/2 - 75, buttonY, 150, buttonHeight);

        // Next button (right)
        nextButton = new Rectangle(screenWidth - 170, buttonY, 120, buttonHeight);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Draw static background & shadow
        batch.draw(game.backgroundStatic, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(game.backgroundRectangle, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(game.shadow, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Draw main title
        game.titleFont.setColor(Color.WHITE);
        layout.setText(game.titleFont, "INSTRUCTIONS");
        game.titleFont.draw(batch, "INSTRUCTIONS",
            (Gdx.graphics.getWidth() - layout.width) / 2,
            Gdx.graphics.getHeight() - 50);


        float contentY = Gdx.graphics.getHeight() - 150;
        float lineHeight = 35;

        // Draw page title
        game.titleFont.setColor(Color.WHITE);
        layout.setText(game.titleFont, PAGE_TITLES[currentPage]);
        game.titleFont.draw(batch, PAGE_TITLES[currentPage],
            (Gdx.graphics.getWidth() - layout.width) / 2,
            contentY);

        // Draw page content
        game.bodyFont.setColor(Color.WHITE);
        contentY -= 120;
        for (String line : PAGE_CONTENT[currentPage]) {
            if (!line.isEmpty()) {
                layout.setText(game.bodyFont, line);
                game.bodyFont.draw(batch, line,
                    (Gdx.graphics.getWidth() - layout.width) / 2,
                    contentY);
            }
            contentY -= lineHeight;
        }

        // Get mouse position for button hover effects
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        // Draw buttons with hover effects
        drawButton("Previous", prevButton, mouseX, mouseY, currentPage > 0);
        drawButton("Back to Menu", backButton, mouseX, mouseY, true);
        drawButton("Next", nextButton, mouseX, mouseY, currentPage < TOTAL_PAGES - 1);

        batch.end();

        // Handle button clicks
        if (Gdx.input.justTouched()) {
            if (prevButton.contains(mouseX, mouseY) && currentPage > 0) {
                currentPage--;
            } else if (nextButton.contains(mouseX, mouseY) && currentPage < TOTAL_PAGES - 1) {
                currentPage++;
            } else if (backButton.contains(mouseX, mouseY)) {
                game.setScreen(new MenuScreen(game));
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






