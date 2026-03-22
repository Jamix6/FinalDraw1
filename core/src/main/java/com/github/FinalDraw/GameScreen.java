package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class GameScreen implements Screen {
    private static final float REF_W = 1280f;
    private static final float REF_H = 720f;

    private final Core game;
    private SpriteBatch batch;
    private GlyphLayout layout;
    private float layoutScale;
    private float layoutOffsetX;
    private float layoutOffsetY;

    //Vars
    private boolean isGameActive;
    private boolean isRoundActive;
    private Rectangle powerupButton;
    private Rectangle powerupPanelBounds;
    private boolean isPowerupPanelOpen;
    private Rectangle optionsButtonBounds;
    private Rectangle optionsPanelBounds;
    private Rectangle optionsRestartBounds;
    private Rectangle optionsChangeLevelBounds;
    private Rectangle optionsHomeBounds;
    private boolean isOptionsOpen;
    private boolean isQuitConfirmOpen;
    private Rectangle quitConfirmPanelBounds;
    private Rectangle quitYesBounds;
    private Rectangle quitNoBounds;
    private boolean isLevelSelectOpen;
    private Rectangle levelSelectPanelBounds;
    private Rectangle levelEasyBounds;
    private Rectangle levelMediumBounds;
    private Rectangle levelHardBounds;
    private boolean isMatchEndOpen;
    private Rectangle matchEndPanelBounds;
    private Rectangle matchEndPlayAgainBounds;
    private Rectangle matchEndHomeBounds;
    private boolean showRoundOutcome;
    private boolean roundOutcomeWin;
    private String roundOutcomeText;

    //Buhay and shi
    private int playerLives;
    private int aiLives;
    private int maxPlayerLives;
    private int maxAILives;
    private int playerShield;
    private int aiShield;
    private int maxShield = 2;

    //Cards
    private Array<Integer> deck;
    private Array<Integer> playerCards;
    private Array<Integer> aiCards;
    private Array<Integer> playerSortedCards;
    private Array<Integer> aiSortedCards;

    //Game elems
    private int playerScore;
    private int aiScore;
    private String gameMessage;

    private Texture gameBg;
    private boolean ownsGameBgTexture;
    private Texture solidPixel;
    private Texture[] healthBarTextures;
    private Texture[] shieldBarTextures;
    private float healthBarWidth;
    private float healthBarHeight;
    private float shieldBarWidth;
    private float shieldBarHeight;

    private Texture[] playerCardTextures;
    private Texture[] aiCardTextures;
    private Texture aiHiddenCardTexture;
    private Texture playerHiddenCardTexture;
    private ShaderProgram cardShader;

    //track mo rounds
    private int currentRound;
    private int playerWins;
    private int aiWins;

    //card display PLACEHOLDER REPLACE DENG HA!
    private float cardSpacing;
    private float cardWidth;
    private float cardHeight;

    // Diff info
    private String difficultyName;

    public GameScreen(Core game) {
        this.game = game;

        // setup diff
        setupDifficulty();

        this.isGameActive = true;
        this.isRoundActive = true;
        this.currentRound = 1;
        this.playerWins = 0;
        this.aiWins = 0;


        this.deck = new Array<>();
        this.playerCards = new Array<>();
        this.aiCards = new Array<>();
        this.playerSortedCards = new Array<>();
        this.aiSortedCards = new Array<>();

        this.gameMessage = "Round 1 - Draw a card or pass!";


        initializeDeck();
    }

    private void setupDifficulty() {
        maxPlayerLives = 7;
        maxAILives = 7;

        switch (game.difficulty) {
            case 0:
                difficultyName = "Easy";
                playerLives = maxPlayerLives;
                aiLives = 3;
                break;
            case 2:
                difficultyName = "Hard";
                playerLives = 3;
                aiLives = maxAILives;
                break;
            case 1:
            default:
                difficultyName = "Medium";
                playerLives = maxPlayerLives;
                aiLives = maxAILives;
                break;
        }

        playerShield = 0;
        aiShield = 0;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        layout = new GlyphLayout();
        ShaderProgram.pedantic = false;
        cardShader = new ShaderProgram(
            "attribute vec4 a_position;\n" +
                "attribute vec4 a_color;\n" +
                "attribute vec2 a_texCoord0;\n" +
                "uniform mat4 u_projTrans;\n" +
                "varying vec4 v_color;\n" +
                "varying vec2 v_texCoords;\n" +
                "void main(){\n" +
                "  v_color = a_color;\n" +
                "  v_color.a = v_color.a * (255.0/254.0);\n" +
                "  v_texCoords = a_texCoord0;\n" +
                "  gl_Position = u_projTrans * a_position;\n" +
                "}\n",
            "#ifdef GL_ES\n" +
                "precision mediump float;\n" +
                "#endif\n" +
                "varying vec4 v_color;\n" +
                "varying vec2 v_texCoords;\n" +
                "uniform sampler2D u_texture;\n" +
                "uniform vec2 u_size;\n" +
                "uniform float u_radius;\n" +
                "float roundedBoxSDF(vec2 p, vec2 b, float r){\n" +
                "  vec2 q = abs(p) - b;\n" +
                "  return length(max(q,0.0)) + min(max(q.x,q.y),0.0) - r;\n" +
                "}\n" +
                "void main(){\n" +
                "  vec2 p = v_texCoords * u_size - 0.5 * u_size;\n" +
                "  float r = min(u_radius, min(u_size.x,u_size.y) * 0.5);\n" +
                "  vec2 b = 0.5 * u_size - vec2(r);\n" +
                "  float d = roundedBoxSDF(p, b, r);\n" +
                "  if (d > 0.0) discard;\n" +
                "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" +
                "}\n"
        );
        if (!cardShader.isCompiled()) {
            cardShader.dispose();
            cardShader = null;
        }

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        solidPixel = new Texture(pixmap);
        pixmap.dispose();

        Texture resolvedBg = null;
        ownsGameBgTexture = false;
        if (Gdx.files.internal("BG/GameBG.png").exists()) {
            resolvedBg = new Texture(Gdx.files.internal("BG/GameBG.png"));
            ownsGameBgTexture = true;
        } else {
            resolvedBg = game.backgroundStatic;
        }
        gameBg = resolvedBg;

        healthBarTextures = new Texture[8];
        for (int i = 0; i <= 7; i++) {
            healthBarTextures[i] = new Texture(Gdx.files.internal("Health and Shield Bar/Health/" + i + "HP.png"));
        }

        shieldBarTextures = new Texture[3];
        for (int i = 0; i <= 2; i++) {
            shieldBarTextures[i] = new Texture(Gdx.files.internal("Health and Shield Bar/Shield/" + i + "SP.png"));
        }

        playerCardTextures = new Texture[12];
        aiCardTextures = new Texture[12];
        for (int i = 1; i <= 11; i++) {
            playerCardTextures[i] = new Texture(Gdx.files.internal("Cards/Blue/b" + i + ".png"));
            aiCardTextures[i] = new Texture(Gdx.files.internal("Cards/Red/r" + i + ".png"));
        }
        aiHiddenCardTexture = new Texture(Gdx.files.internal("Cards/Red/rhiddencard.png"));
        playerHiddenCardTexture = new Texture(Gdx.files.internal("Cards/Blue/bhiddencard.png"));

        // Init at exit
        updateButtonPositions();

        // neu round
        resetRound();
    }

    private void updateButtonPositions() {
        healthBarWidth = 600f;
        healthBarHeight = 10f;
        shieldBarWidth = 300f;
        shieldBarHeight = 6f;

        cardHeight = 190f;
        cardWidth = cardHeight * (405f / 570f);
        cardSpacing = cardWidth + 28f;

        powerupButton = new Rectangle(74f, 150f, 120f, 120f);
        powerupPanelBounds = new Rectangle(410f, 250f, 460f, 220f);

        optionsButtonBounds = new Rectangle(1120f, 665f, 130f, 40f);
        optionsPanelBounds = new Rectangle(880f, 470f, 340f, 210f);
        optionsRestartBounds = new Rectangle(905f, 605f, 290f, 40f);
        optionsChangeLevelBounds = new Rectangle(905f, 555f, 290f, 40f);
        optionsHomeBounds = new Rectangle(905f, 505f, 290f, 40f);

        quitConfirmPanelBounds = new Rectangle(420f, 265f, 440f, 190f);
        quitYesBounds = new Rectangle(465f, 290f, 170f, 50f);
        quitNoBounds = new Rectangle(645f, 290f, 170f, 50f);

        levelSelectPanelBounds = new Rectangle(420f, 245f, 440f, 230f);
        levelEasyBounds = new Rectangle(470f, 400f, 340f, 45f);
        levelMediumBounds = new Rectangle(470f, 340f, 340f, 45f);
        levelHardBounds = new Rectangle(470f, 280f, 340f, 45f);

        matchEndPanelBounds = new Rectangle(420f, 255f, 440f, 210f);
        matchEndPlayAgainBounds = new Rectangle(470f, 345f, 340f, 50f);
        matchEndHomeBounds = new Rectangle(470f, 285f, 340f, 50f);
    }

    private void updateLayoutTransform() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        layoutScale = Math.min(w / REF_W, h / REF_H);
        layoutOffsetX = (w - REF_W * layoutScale) / 2f;
        layoutOffsetY = (h - REF_H * layoutScale) / 2f;
    }

    private float sx(float x) {
        return layoutOffsetX + x * layoutScale;
    }

    private float sy(float y) {
        return layoutOffsetY + y * layoutScale;
    }

    private float ss(float v) {
        return v * layoutScale;
    }

    private float toRefX(float screenX) {
        return (screenX - layoutOffsetX) / layoutScale;
    }

    private float toRefY(float screenY) {
        return (screenY - layoutOffsetY) / layoutScale;
    }

    private void initializeDeck() {
        deck.clear();
        //gawa ng deck, 11 limits
        for (int i = 1; i <= 11; i++) {
            deck.add(i);
        }
        shuffleDeck();
    }

    private void shuffleDeck() {
        // algo para mag shuffle, trust trust
        for (int i = 0; i < deck.size; i++) {
            int randomIndex = (int)(Math.random() * deck.size);
            int temp = deck.get(i);
            deck.set(i, deck.get(randomIndex));
            deck.set(randomIndex, temp);
        }
    }

    private void resetRound() {
        playerCards.clear();
        aiCards.clear();
        playerSortedCards.clear();
        aiSortedCards.clear();
        showRoundOutcome = false;
        roundOutcomeText = null;

        // Reset deck for new round
        initializeDeck();

        // Draw initial cards (2 for player, 2 for AI)
        drawInitialCards();

        isRoundActive = true;
        gameMessage = "Round " + currentRound + " - Draw a card or pass!";
    }

    private void drawInitialCards() {
        // Player draws 2 cards (first card hidden from AI)
        for (int i = 0; i < 2; i++) {
            if (!deck.isEmpty()) {
                int card = deck.pop();
                playerCards.add(card);
            }
        }

        // AI draws 2 cards
        for (int i = 0; i < 2; i++) {
            if (!deck.isEmpty()) {
                int card = deck.pop();
                aiCards.add(card);
            }
        }

        // Sort the cards using insertion sort
        insertionSort(playerCards, playerSortedCards);
        insertionSort(aiCards, aiSortedCards);

        // Calculate scores
        calculateScores();
    }

    // INSERTION SORT IMPLEMENTATION
    private void insertionSort(Array<Integer> original, Array<Integer> sorted) {
        sorted.clear();

        // Copy original array
        for (int i = 0; i < original.size; i++) {
            sorted.add(original.get(i));
        }

        // Perform insertion sort
        for (int i = 1; i < sorted.size; i++) {
            int key = sorted.get(i);
            int j = i - 1;

            // Move elements of sorted[0..i-1] that are greater than key
            // to one position ahead of their current position
            // basta yun Insert
            while (j >= 0 && sorted.get(j) > key) {
                sorted.set(j + 1, sorted.get(j));
                j = j - 1;
            }
            sorted.set(j + 1, key);
        }
    }

    private void calculateScores() {
        playerScore = 0;
        for (int card : playerCards) {
            playerScore += card;
        }

        aiScore = 0;
        for (int card : aiCards) {
            aiScore += card;
        }
    }

    private int getAIRevealedScore() {
        int revealedScore = 0;
        if (aiSortedCards == null) {
            return 0;
        }
        for (int i = 1; i < aiSortedCards.size; i++) {
            revealedScore += aiSortedCards.get(i);
        }
        return revealedScore;
    }

    private void resetGame() {
        // Reset lives based on current difficulty
        setupDifficulty();
        currentRound = 1;
        playerWins = 0;
        aiWins = 0;
        showRoundOutcome = false;
        roundOutcomeText = null;
        isMatchEndOpen = false;
        isLevelSelectOpen = false;
        resetRound();
        isGameActive = true;
    }

    private int getBarIndex(int currentValue, int maxValue, int maxIndex) {
        if (maxValue <= 0) {
            return 0;
        }
        float ratio = currentValue / (float) maxValue;
        int index = Math.round(ratio * maxIndex);
        if (index < 0) return 0;
        if (index > maxIndex) return maxIndex;
        return index;
    }

    private String toRoman(int number) {
        if (number <= 0) return "0";
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder result = new StringBuilder();
        int remaining = number;
        for (int i = 0; i < values.length; i++) {
            while (remaining >= values[i]) {
                remaining -= values[i];
                result.append(symbols[i]);
            }
        }
        return result.toString();
    }

    private void drawHealthBar(float x, float y, int currentHealth, int maxHealth) {
        if (healthBarTextures == null || healthBarTextures.length != 8) return;
        int index = getBarIndex(currentHealth, maxHealth, 7);
        Texture texture = healthBarTextures[index];
        if (texture == null) return;
        batch.setColor(Color.WHITE);
        batch.draw(texture, sx(x), sy(y), ss(healthBarWidth), ss(healthBarHeight));
    }

    private void drawShieldBar(float x, float y, int currentShield, int maxShield) {
        if (shieldBarTextures == null || shieldBarTextures.length != 3) return;
        int index = getBarIndex(currentShield, maxShield, 2);
        Texture texture = shieldBarTextures[index];
        if (texture == null) return;
        batch.setColor(Color.WHITE);
        batch.draw(texture, sx(x), sy(y), ss(shieldBarWidth), ss(shieldBarHeight));
    }

    private void drawRoundedTexture(Texture texture, float x, float y, float width, float height, float radius) {
        if (texture == null) return;
        float dx = sx(x);
        float dy = sy(y);
        float dw = ss(width);
        float dh = ss(height);
        float rr = ss(radius);
        if (cardShader == null) {
            batch.setColor(Color.WHITE);
            batch.draw(texture, dx, dy, dw, dh);
            return;
        }
        batch.flush();
        batch.setShader(cardShader);
        cardShader.setUniformf("u_size", dw, dh);
        cardShader.setUniformf("u_radius", rr);
        batch.setColor(Color.WHITE);
        batch.draw(texture, dx, dy, dw, dh);
    }

    private void drawCard(float x, float y, int cardValue, boolean isPlayer, boolean isHidden) {
        float dx = sx(x);
        float dy = sy(y);
        float dw = ss(cardWidth);
        float dh = ss(cardHeight);
        float border = ss(3f);
        float radius = ss(13f);

        if (cardShader != null) {
            batch.flush();
            batch.setShader(cardShader);
            cardShader.setUniformf("u_size", dw + border * 2f, dh + border * 2f);
            cardShader.setUniformf("u_radius", radius + border);
        }
        batch.setColor(Color.WHITE);
        batch.draw(game.backgroundRectangle, dx - border, dy - border, dw + border * 2f, dh + border * 2f);

        Texture texture;
        if (isHidden) {
            texture = aiHiddenCardTexture;
        } else {
            Texture[] textures = isPlayer ? playerCardTextures : aiCardTextures;
            if (textures == null || cardValue < 1 || cardValue >= textures.length) return;
            texture = textures[cardValue];
        }
        if (texture == null) return;

        if (cardShader != null) {
            batch.flush();
            cardShader.setUniformf("u_size", dw, dh);
            cardShader.setUniformf("u_radius", radius);
        }
        batch.setColor(Color.WHITE);
        batch.draw(texture, dx, dy, dw, dh);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        updateLayoutTransform();

        batch.setColor(Color.WHITE);
        batch.draw(gameBg, sx(0), sy(0), ss(REF_W), ss(REF_H));

        float barX = 290f;
        float aiHealthY = 654f;
        float playerHealthY = 48f;
        barX = 400f;

        drawHealthBar(barX, aiHealthY, aiLives, maxAILives);
        if (aiShield > 0) {
            drawShieldBar(barX, aiHealthY - 12f, aiShield, maxShield);
        }

        drawHealthBar(barX, playerHealthY, playerLives, maxPlayerLives);
        if (playerShield > 0) {
            drawShieldBar(barX, playerHealthY + 24f, playerShield, maxShield);
        }

        if (aiHiddenCardTexture != null) {
            float deckH = 140f;
            float deckW = deckH * (405f / 570f);
            drawRoundedTexture(aiHiddenCardTexture, 80f, 420f, deckW, deckH, 12f);
        }
        if (playerHiddenCardTexture != null) {
            float deckH = 140f;
            float deckW = deckH * (405f / 570f);
            drawRoundedTexture(playerHiddenCardTexture, 1125f, 180f, deckW, deckH, 12f);
        }

        float centerX = 640f;
        float aiCardsY = 435f;
        float playerCardsY = 102f;

        float gap = cardSpacing - cardWidth;
        float aiTotalWidth = aiSortedCards.size * cardWidth + Math.max(0, aiSortedCards.size - 1) * gap;
        float aiStartX = centerX - aiTotalWidth / 2f;
        for (int i = 0; i < aiSortedCards.size; i++) {
            boolean isHidden = (i == 0 && isRoundActive);
            drawCard(aiStartX + i * (cardWidth + gap), aiCardsY, aiSortedCards.get(i), false, isHidden);
        }

        float playerTotalWidth = playerSortedCards.size * cardWidth + Math.max(0, playerSortedCards.size - 1) * gap;
        float playerStartX = centerX - playerTotalWidth / 2f;
        for (int i = 0; i < playerSortedCards.size; i++) {
            drawCard(playerStartX + i * (cardWidth + gap), playerCardsY, playerSortedCards.get(i), true, false);
        }

        if (cardShader != null) {
            batch.flush();
            batch.setShader(null);
        }

        float prevTitleScaleX = game.titleFont.getData().scaleX;
        float prevTitleScaleY = game.titleFont.getData().scaleY;
        game.titleFont.getData().setScale(layoutScale * 0.42f);

        game.titleFont.setColor(Color.GOLD);
        String roundText = "ROUND " + toRoman(currentRound) + " " + difficultyName.toUpperCase();
        layout.setText(game.titleFont, roundText);
        game.titleFont.draw(batch, roundText, sx(centerX) - layout.width / 2f, sy(387f));

        game.titleFont.getData().setScale(layoutScale * 0.55f);

        String playerScoreText = playerScore + "/21";
        String aiScoreText = isRoundActive ? "? + " + getAIRevealedScore() + "/21" : aiScore + "/21";
        String vsText = "VS";

        game.titleFont.setColor(Color.GOLD);
        layout.setText(game.titleFont, playerScoreText);
        game.titleFont.draw(batch, playerScoreText, sx(470f) - layout.width / 2f, sy(361f));

        layout.setText(game.titleFont, vsText);
        game.titleFont.draw(batch, vsText, sx(centerX) - layout.width / 2f, sy(361f));

        layout.setText(game.titleFont, aiScoreText);
        game.titleFont.draw(batch, aiScoreText, sx(810f) - layout.width / 2f, sy(361f));

        game.titleFont.getData().setScale(prevTitleScaleX, prevTitleScaleY);

        if (isPowerupPanelOpen) {
            batch.setColor(0f, 0f, 0f, 0.55f);
            batch.draw(solidPixel != null ? solidPixel : game.backgroundRectangle, sx(0), sy(0), ss(REF_W), ss(REF_H));
            batch.setColor(0.25f, 0.25f, 0.25f, 0.95f);
            batch.draw(solidPixel != null ? solidPixel : game.backgroundRectangle, sx(powerupPanelBounds.x), sy(powerupPanelBounds.y), ss(powerupPanelBounds.width), ss(powerupPanelBounds.height));
            batch.setColor(Color.WHITE);
            game.titleFont.setColor(Color.WHITE);
            String panelTitle = "POWER UPS";
            float prevScaleX = game.titleFont.getData().scaleX;
            float prevScaleY = game.titleFont.getData().scaleY;
            game.titleFont.getData().setScale(layoutScale * 0.6f);
            layout.setText(game.titleFont, panelTitle);
            game.titleFont.draw(batch, panelTitle, sx(powerupPanelBounds.x + powerupPanelBounds.width / 2f) - layout.width / 2f, sy(powerupPanelBounds.y + powerupPanelBounds.height - 30f));
            game.titleFont.getData().setScale(prevScaleX, prevScaleY);
        }

        if (showRoundOutcome && !isRoundActive && isGameActive) {
            if (roundOutcomeWin) {
                batch.setColor(0f, 0f, 0f, 0.30f);
            } else {
                batch.setColor(1f, 0f, 0f, 0.30f);
            }
            batch.draw(solidPixel != null ? solidPixel : game.backgroundRectangle, sx(0), sy(0), ss(REF_W), ss(REF_H));
            batch.setColor(Color.WHITE);

            float prevScaleX = game.titleFont.getData().scaleX;
            float prevScaleY = game.titleFont.getData().scaleY;
            game.titleFont.getData().setScale(layoutScale * 0.85f);
            game.titleFont.setColor(Color.WHITE);
            String text = roundOutcomeText == null ? "" : roundOutcomeText;
            layout.setText(game.titleFont, text);
            game.titleFont.draw(batch, text, sx(REF_W / 2f) - layout.width / 2f, sy(REF_H / 2f + 10f));
            game.titleFont.getData().setScale(prevScaleX, prevScaleY);
        }

        if (optionsButtonBounds != null) {
            float prevScaleX = game.bodyFont.getData().scaleX;
            float prevScaleY = game.bodyFont.getData().scaleY;
            game.bodyFont.getData().setScale(layoutScale * 0.9f);
            game.bodyFont.setColor(Color.WHITE);
            String optionsText = "OPTIONS";
            layout.setText(game.bodyFont, optionsText);
            float ox = optionsButtonBounds.x + (optionsButtonBounds.width - layout.width) / 2f;
            float oy = optionsButtonBounds.y + (optionsButtonBounds.height + layout.height) / 2f;
            game.bodyFont.draw(batch, optionsText, sx(ox), sy(oy));
            game.bodyFont.getData().setScale(prevScaleX, prevScaleY);
        }

        if (isOptionsOpen && solidPixel != null) {
            batch.setColor(0f, 0f, 0f, 0.50f);
            batch.draw(solidPixel, sx(0), sy(0), ss(REF_W), ss(REF_H));
            batch.setColor(0.1f, 0.1f, 0.1f, 0.95f);
            batch.draw(solidPixel, sx(optionsPanelBounds.x), sy(optionsPanelBounds.y), ss(optionsPanelBounds.width), ss(optionsPanelBounds.height));
            batch.setColor(Color.WHITE);

            float prevScaleX = game.titleFont.getData().scaleX;
            float prevScaleY = game.titleFont.getData().scaleY;
            game.titleFont.getData().setScale(layoutScale * 0.55f);
            game.titleFont.setColor(Color.WHITE);
            String header = "OPTIONS";
            layout.setText(game.titleFont, header);
            game.titleFont.draw(batch, header, sx(optionsPanelBounds.x + optionsPanelBounds.width / 2f) - layout.width / 2f, sy(optionsPanelBounds.y + optionsPanelBounds.height - 25f));
            game.titleFont.getData().setScale(prevScaleX, prevScaleY);

            float prevBodyScaleX = game.bodyFont.getData().scaleX;
            float prevBodyScaleY = game.bodyFont.getData().scaleY;
            game.bodyFont.getData().setScale(layoutScale * 0.9f);

            game.bodyFont.setColor(Color.WHITE);
            layout.setText(game.bodyFont, "Restart");
            game.bodyFont.draw(batch, "Restart", sx(optionsRestartBounds.x), sy(optionsRestartBounds.y + 28f));

            layout.setText(game.bodyFont, "Change Level");
            game.bodyFont.draw(batch, "Change Level", sx(optionsChangeLevelBounds.x), sy(optionsChangeLevelBounds.y + 28f));

            layout.setText(game.bodyFont, "Home / Menu");
            game.bodyFont.draw(batch, "Home / Menu", sx(optionsHomeBounds.x), sy(optionsHomeBounds.y + 28f));

            game.bodyFont.getData().setScale(prevBodyScaleX, prevBodyScaleY);
        }

        if (isQuitConfirmOpen && solidPixel != null) {
            batch.setColor(0f, 0f, 0f, 0.50f);
            batch.draw(solidPixel, sx(0), sy(0), ss(REF_W), ss(REF_H));
            batch.setColor(0.12f, 0.12f, 0.12f, 0.95f);
            batch.draw(solidPixel, sx(quitConfirmPanelBounds.x), sy(quitConfirmPanelBounds.y), ss(quitConfirmPanelBounds.width), ss(quitConfirmPanelBounds.height));
            batch.setColor(Color.WHITE);

            float prevScaleX = game.titleFont.getData().scaleX;
            float prevScaleY = game.titleFont.getData().scaleY;
            game.titleFont.getData().setScale(layoutScale * 0.55f);
            game.titleFont.setColor(Color.WHITE);
            String prompt = "Do you want to quit?";
            layout.setText(game.titleFont, prompt);
            game.titleFont.draw(batch, prompt, sx(quitConfirmPanelBounds.x + quitConfirmPanelBounds.width / 2f) - layout.width / 2f, sy(quitConfirmPanelBounds.y + quitConfirmPanelBounds.height - 35f));
            game.titleFont.getData().setScale(prevScaleX, prevScaleY);

            float prevBodyScaleX = game.bodyFont.getData().scaleX;
            float prevBodyScaleY = game.bodyFont.getData().scaleY;
            game.bodyFont.getData().setScale(layoutScale * 0.9f);
            game.bodyFont.setColor(Color.WHITE);

            batch.setColor(0.25f, 0.25f, 0.25f, 0.95f);
            batch.draw(solidPixel, sx(quitYesBounds.x), sy(quitYesBounds.y), ss(quitYesBounds.width), ss(quitYesBounds.height));
            batch.draw(solidPixel, sx(quitNoBounds.x), sy(quitNoBounds.y), ss(quitNoBounds.width), ss(quitNoBounds.height));
            batch.setColor(Color.WHITE);

            layout.setText(game.bodyFont, "Yes");
            game.bodyFont.draw(batch, "Yes", sx(quitYesBounds.x + (quitYesBounds.width - layout.width) / 2f), sy(quitYesBounds.y + 34f));

            layout.setText(game.bodyFont, "No");
            game.bodyFont.draw(batch, "No", sx(quitNoBounds.x + (quitNoBounds.width - layout.width) / 2f), sy(quitNoBounds.y + 34f));

            game.bodyFont.getData().setScale(prevBodyScaleX, prevBodyScaleY);
        }

        if (isLevelSelectOpen && solidPixel != null) {
            batch.setColor(0f, 0f, 0f, 0.50f);
            batch.draw(solidPixel, sx(0), sy(0), ss(REF_W), ss(REF_H));
            batch.setColor(0.12f, 0.12f, 0.12f, 0.95f);
            batch.draw(solidPixel, sx(levelSelectPanelBounds.x), sy(levelSelectPanelBounds.y), ss(levelSelectPanelBounds.width), ss(levelSelectPanelBounds.height));
            batch.setColor(Color.WHITE);

            float prevScaleX = game.titleFont.getData().scaleX;
            float prevScaleY = game.titleFont.getData().scaleY;
            game.titleFont.getData().setScale(layoutScale * 0.55f);
            game.titleFont.setColor(Color.WHITE);
            String header = "SELECT LEVEL";
            layout.setText(game.titleFont, header);
            game.titleFont.draw(batch, header, sx(levelSelectPanelBounds.x + levelSelectPanelBounds.width / 2f) - layout.width / 2f, sy(levelSelectPanelBounds.y + levelSelectPanelBounds.height - 25f));
            game.titleFont.getData().setScale(prevScaleX, prevScaleY);

            float prevBodyScaleX = game.bodyFont.getData().scaleX;
            float prevBodyScaleY = game.bodyFont.getData().scaleY;
            game.bodyFont.getData().setScale(layoutScale * 0.9f);
            game.bodyFont.setColor(Color.WHITE);
            game.bodyFont.draw(batch, "Easy", sx(levelEasyBounds.x), sy(levelEasyBounds.y + 32f));
            game.bodyFont.draw(batch, "Medium", sx(levelMediumBounds.x), sy(levelMediumBounds.y + 32f));
            game.bodyFont.draw(batch, "Hard", sx(levelHardBounds.x), sy(levelHardBounds.y + 32f));
            game.bodyFont.getData().setScale(prevBodyScaleX, prevBodyScaleY);
        }

        if (isMatchEndOpen && solidPixel != null) {
            batch.setColor(0f, 0f, 0f, 0.50f);
            batch.draw(solidPixel, sx(0), sy(0), ss(REF_W), ss(REF_H));
            batch.setColor(0.12f, 0.12f, 0.12f, 0.95f);
            batch.draw(solidPixel, sx(matchEndPanelBounds.x), sy(matchEndPanelBounds.y), ss(matchEndPanelBounds.width), ss(matchEndPanelBounds.height));
            batch.setColor(Color.WHITE);

            float prevScaleX = game.titleFont.getData().scaleX;
            float prevScaleY = game.titleFont.getData().scaleY;
            game.titleFont.getData().setScale(layoutScale * 0.55f);
            game.titleFont.setColor(Color.WHITE);
            String header = "MATCH ENDED";
            layout.setText(game.titleFont, header);
            game.titleFont.draw(batch, header, sx(matchEndPanelBounds.x + matchEndPanelBounds.width / 2f) - layout.width / 2f, sy(matchEndPanelBounds.y + matchEndPanelBounds.height - 25f));
            game.titleFont.getData().setScale(prevScaleX, prevScaleY);

            float prevBodyScaleX = game.bodyFont.getData().scaleX;
            float prevBodyScaleY = game.bodyFont.getData().scaleY;
            game.bodyFont.getData().setScale(layoutScale * 0.9f);
            game.bodyFont.setColor(Color.WHITE);
            game.bodyFont.draw(batch, "Play Again", sx(matchEndPlayAgainBounds.x), sy(matchEndPlayAgainBounds.y + 34f));
            game.bodyFont.draw(batch, "Home", sx(matchEndHomeBounds.x), sy(matchEndHomeBounds.y + 34f));
            game.bodyFont.getData().setScale(prevBodyScaleX, prevBodyScaleY);
        }

        batch.end();

        handleGameInput();

        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        handleMouseInput(mouseX, mouseY);
    }

    private void handleGameInput() {
        if (isMatchEndOpen) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
                isMatchEndOpen = false;
            }
            return;
        }
        if (isLevelSelectOpen) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
                isLevelSelectOpen = false;
            }
            return;
        }
        if (isQuitConfirmOpen) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
                isQuitConfirmOpen = false;
            }
            return;
        }
        if (isOptionsOpen) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
                isOptionsOpen = false;
            }
            return;
        }
        if (isPowerupPanelOpen) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
                isPowerupPanelOpen = false;
            }
            return;
        }
        if (!isGameActive) {

            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
                resetGame();
            }
        } else if (isRoundActive) {

            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
                drawCardForPlayer();
            }

            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.P)) {
                passTurn();
            }
        } else {

            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
                startNextRound();
            }
        }


        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) returnToMenu();
    }

    private void handleMouseInput(float mouseX, float mouseY) {
        if (Gdx.input.justTouched()) {
            float refMouseX = toRefX(mouseX);
            float refMouseY = toRefY(mouseY);
            if (isMatchEndOpen) {
                if (matchEndPlayAgainBounds.contains(refMouseX, refMouseY)) {
                    isMatchEndOpen = false;
                    isLevelSelectOpen = true;
                    return;
                }
                if (matchEndHomeBounds.contains(refMouseX, refMouseY)) {
                    isMatchEndOpen = false;
                    returnToMenu();
                    return;
                }
                if (!matchEndPanelBounds.contains(refMouseX, refMouseY)) {
                    isMatchEndOpen = false;
                }
                return;
            }
            if (isLevelSelectOpen) {
                if (levelEasyBounds.contains(refMouseX, refMouseY)) {
                    game.difficulty = 0;
                    resetGame();
                    isLevelSelectOpen = false;
                    isMatchEndOpen = false;
                    return;
                }
                if (levelMediumBounds.contains(refMouseX, refMouseY)) {
                    game.difficulty = 1;
                    resetGame();
                    isLevelSelectOpen = false;
                    isMatchEndOpen = false;
                    return;
                }
                if (levelHardBounds.contains(refMouseX, refMouseY)) {
                    game.difficulty = 2;
                    resetGame();
                    isLevelSelectOpen = false;
                    isMatchEndOpen = false;
                    return;
                }
                if (!levelSelectPanelBounds.contains(refMouseX, refMouseY)) {
                    isLevelSelectOpen = false;
                }
                return;
            }
            if (isQuitConfirmOpen) {
                if (quitYesBounds.contains(refMouseX, refMouseY)) {
                    isQuitConfirmOpen = false;
                    isOptionsOpen = false;
                    returnToMenu();
                    return;
                }
                if (quitNoBounds.contains(refMouseX, refMouseY) || !quitConfirmPanelBounds.contains(refMouseX, refMouseY)) {
                    isQuitConfirmOpen = false;
                    return;
                }
                return;
            }
            if (isOptionsOpen) {
                if (optionsRestartBounds.contains(refMouseX, refMouseY)) {
                    isOptionsOpen = false;
                    isQuitConfirmOpen = false;
                    isPowerupPanelOpen = false;
                    showRoundOutcome = false;
                    roundOutcomeText = null;
                    resetGame();
                    return;
                }
                if (optionsChangeLevelBounds.contains(refMouseX, refMouseY)) {
                    isOptionsOpen = false;
                    isQuitConfirmOpen = false;
                    isPowerupPanelOpen = false;
                    isMatchEndOpen = false;
                    isLevelSelectOpen = true;
                    return;
                }
                if (optionsHomeBounds.contains(refMouseX, refMouseY)) {
                    isQuitConfirmOpen = true;
                    return;
                }
                if (!optionsPanelBounds.contains(refMouseX, refMouseY)) {
                    isOptionsOpen = false;
                }
                return;
            }
            if (isPowerupPanelOpen) {
                if (!powerupPanelBounds.contains(refMouseX, refMouseY)) {
                    isPowerupPanelOpen = false;
                }
                return;
            }
            if (optionsButtonBounds != null && optionsButtonBounds.contains(refMouseX, refMouseY)) {
                isOptionsOpen = !isOptionsOpen;
                return;
            }
            if (powerupButton != null && powerupButton.contains(refMouseX, refMouseY)) {
                isPowerupPanelOpen = true;
                return;
            }
        }
    }

    private void drawCardForPlayer() {
        if (!isRoundActive || !isGameActive) return;


        if (deck.isEmpty()) {
            gameMessage = "No more cards in deck!";
            return;
        }

        // Player draws a card
        int card = deck.pop();
        playerCards.add(card);

        //INSERTION SORT PALDO DIBA
        insertionSort(playerCards, playerSortedCards);


        calculateScores();


        aiTurn();

        gameMessage = "You drew card: " + card + " (Total: " + playerScore + ")";


        checkRoundEnd();
    }

    private void aiTurn() {
        // Simple AI logic based on score and deck size
        if (deck.isEmpty()) return;

        // AI draws if:
        // 1. Score < 15 (always draw)
        // 2. Score between 15-18 (70% chance)
        // 3. Score > 18 (30% chance)
        boolean shouldDraw = false;

        if (aiScore < 15) {
            shouldDraw = true;
        } else if (aiScore < 18) {
            shouldDraw = Math.random() > 0.3; // 70% chance
        } else {
            shouldDraw = Math.random() > 0.7; // 30% chance
        }

        if (shouldDraw) {
            int card = deck.pop();
            aiCards.add(card);


            insertionSort(aiCards, aiSortedCards);


            calculateScores();

            // Only show revealed cards in message
            int revealedScore = getAIRevealedScore();
            int newCardIndex = aiCards.size - 1;


            if (newCardIndex > 0) {
                gameMessage += " | AI drew card: " + card + " (Visible: " + revealedScore + ")";
            } else {
                gameMessage += " | AI drew a hidden card";
            }
        } else {
            gameMessage += " | AI passed";
        }
    }

    private void passTurn() {
        if (!isRoundActive || !isGameActive) return;


        gameMessage = "You passed.";


        aiTurn();


        isRoundActive = false;
        endRound();
    }

    private void checkRoundEnd() {

        boolean playerBust = playerScore > 21;
        boolean aiBust = aiScore > 21;

        if (playerBust || aiBust || deck.isEmpty()) {
            isRoundActive = false;
            endRound();
        }
    }

    private void endRound() {
        // Reveal all cards by removing hidden status

        int prevPlayerLives = playerLives;
        int prevAiLives = aiLives;
        String resultMessage;

        if (playerScore > 21 && aiScore > 21) {
            // Both bust - closer to 21 loses a life
            int playerDiff = Math.abs(21 - playerScore);
            int aiDiff = Math.abs(21 - aiScore);

            if (playerDiff < aiDiff) {
                // Player closer to 21 - AI loses life
                aiLives--;
                playerWins++;
                resultMessage = "Both bust! You win (closer to 21)! AI loses a life.";
            } else if (aiDiff < playerDiff) {
                // AI closer to 21 - Player loses life
                playerLives--;
                aiWins++;
                resultMessage = "Both bust! AI wins (closer to 21)! You lose a life.";
            } else {
                // Tie - no life loss
                resultMessage = "Draw! No lives lost.";
            }
        } else if (playerScore > 21) {
            // Player busts - loses life
            playerLives--;
            aiWins++;
            resultMessage = "You bust! You lose a life.";
        } else if (aiScore > 21) {
            // AI busts - loses life
            aiLives--;
            playerWins++;
            resultMessage = "AI busts! AI loses a life.";
        } else if (playerScore == aiScore) {
            // Tie - no life loss
            resultMessage = "Draw! No lives lost.";
        } else if (Math.abs(21 - playerScore) < Math.abs(21 - aiScore)) {
            // Player closer to 21 - AI loses life
            aiLives--;
            playerWins++;
            resultMessage = "You win! " + playerScore + " vs " + aiScore + ". AI loses a life.";
        } else {
            // AI closer to 21 - Player loses life
            playerLives--;
            aiWins++;
            resultMessage = "AI wins! " + aiScore + " vs " + playerScore + ". You lose a life.";
        }

        // Add card info to message
        resultMessage += " Cards: You [" + formatCards(playerSortedCards) + "] vs AI [" + formatCards(aiSortedCards) + "]";
        gameMessage = resultMessage + " Press SPACE for next round.";

        if (playerLives < prevPlayerLives) {
            showRoundOutcome = true;
            roundOutcomeWin = false;
            roundOutcomeText = "-1HP you loss";
        } else if (aiLives < prevAiLives) {
            showRoundOutcome = true;
            roundOutcomeWin = true;
            roundOutcomeText = "You survived";
        } else {
            showRoundOutcome = false;
            roundOutcomeText = null;
        }

        // Check for game over
        if (playerLives <= 0 || aiLives <= 0) {
            endGame();
        }
    }

    private String formatCards(Array<Integer> cards) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size; i++) {
            sb.append(cards.get(i));
            if (i < cards.size - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private void startNextRound() {
        if (!isGameActive) return;

        currentRound++;
        resetRound();
    }

    private void endGame() {
        isGameActive = false;
        isRoundActive = false;
        isMatchEndOpen = true;
        isOptionsOpen = false;
        isQuitConfirmOpen = false;
        isPowerupPanelOpen = false;
        isLevelSelectOpen = false;

        if (playerLives <= 0) {
            gameMessage = "GAME OVER! AI wins! Press SPACE to restart.";
        } else {
            gameMessage = "VICTORY! You win! Press SPACE to restart.";
        }
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
    public void pause() {

        if (isGameActive && isRoundActive) {
            gameMessage = "Game Paused";
        }
    }

    @Override
    public void resume() {

        if (isGameActive && isRoundActive) {
            gameMessage = "Round " + currentRound + " - Game Resumed";
        }
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (ownsGameBgTexture && gameBg != null) {
            gameBg.dispose();
        }
        gameBg = null;
        ownsGameBgTexture = false;
        if (solidPixel != null) {
            solidPixel.dispose();
            solidPixel = null;
        }

        if (healthBarTextures != null) {
            for (Texture texture : healthBarTextures) {
                if (texture != null) texture.dispose();
            }
            healthBarTextures = null;
        }
        if (shieldBarTextures != null) {
            for (Texture texture : shieldBarTextures) {
                if (texture != null) texture.dispose();
            }
            shieldBarTextures = null;
        }
        if (playerCardTextures != null) {
            for (Texture texture : playerCardTextures) {
                if (texture != null) texture.dispose();
            }
            playerCardTextures = null;
        }
        if (aiCardTextures != null) {
            for (Texture texture : aiCardTextures) {
                if (texture != null) texture.dispose();
            }
            aiCardTextures = null;
        }
        if (aiHiddenCardTexture != null) {
            aiHiddenCardTexture.dispose();
            aiHiddenCardTexture = null;
        }
        if (playerHiddenCardTexture != null) {
            playerHiddenCardTexture.dispose();
            playerHiddenCardTexture = null;
        }
        if (cardShader != null) {
            cardShader.dispose();
            cardShader = null;
        }
    }
}
