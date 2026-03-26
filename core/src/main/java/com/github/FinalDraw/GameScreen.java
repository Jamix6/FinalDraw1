package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
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
    private static final int TARGET_MOD_OVERRIDE = 1;
    private static final int TARGET_MOD_DELTA = 2;

    private static final class TargetModifier {
        private final int kind;
        private final int value;

        private TargetModifier(int kind, int value) {
            this.kind = kind;
            this.value = value;
        }
    }

    private enum PowerupType {
        GO_FOR_17("Go for 17"),
        GO_FOR_24("Go for 24"),
        PROTECTION("Protection"),
        REMOVAL("Removal"),
        RESHUFFLE("Reshuffle"),
        DOUBLE_DOWN("Double Down"),
        FORESIGHT("Foresight"),
        SWAP("Swap"),
        OVERLOAD("Overload"),
        DISCARD("Discard"),
        TOSS("Toss"),
        CLEAR("Clear");

        private final String label;

        PowerupType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

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
    private boolean matchWon;
    private Rectangle matchEndPanelBounds;
    private Rectangle matchEndPlayAgainBounds;
    private Rectangle matchEndHomeBounds;
    private boolean showRoundOutcome;
    private boolean roundOutcomeWin;
    private boolean roundOutcomeDraw;
    private String roundOutcomeText;

    //Buhay and shi
    private int playerLives;
    private int aiLives;
    private int maxPlayerLives;
    private int maxAILives;
    private int playerShield;
    private int aiShield;
    private int maxShield = 2;
    private int playerShieldRoundsLeft;
    private int aiShieldRoundsLeft;

    private int baseTarget = 21;
    private int playerTarget;
    private int aiTarget;
    private int playerTargetOverride;
    private int aiTargetOverride;
    private int playerTargetDelta;
    private int aiTargetDelta;
    private boolean playerForesightActive;
    private boolean aiForesightActive;
    private Array<TargetModifier> playerTargetModifiers = new Array<>();
    private Array<TargetModifier> aiTargetModifiers = new Array<>();

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
    private Texture aiDeckTexture;
    private Texture playerDeckTexture;
    private ShaderProgram cardShader;
    private Texture powerupPanelBg;
    private Music[] aiDrawVoicesEasy;
    private Music[] aiPassVoicesEasy;
    private Music[] aiDrawVoicesMedium;
    private Music[] aiPassVoicesMedium;
    private Music[] aiDrawVoicesHard;
    private Music[] aiPassVoicesHard;
    private Music currentAIVoice;
    private int lastDrawIdx = -1;
    private int lastPassIdx = -1;
    private boolean isAITurnPending;
    private float aiTurnDelaySeconds = 0.8f;
    private float aiTurnDelayRemaining;
    private int aiPostAction;
    private String turnLabel;
    private boolean aiPlannedShouldDraw;
    private Array<String> aiPowerupNotifications;

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

    private Array<PowerupType> playerRoundPowerups = new Array<>();
    private Array<PowerupType> aiRoundPowerups = new Array<>();
    private Array<PowerupType> playerInventoryPowerups = new Array<>();
    private Array<PowerupType> aiInventoryPowerups = new Array<>();
    private int powerupsUsedThisRound;
    private Array<Rectangle> powerupClickBounds = new Array<>();
    private Array<PowerupType> powerupClickTypes = new Array<>();
    private Array<Integer> powerupClickSources = new Array<>();

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
        this.aiPowerupNotifications = new Array<>();

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
        playerShieldRoundsLeft = 0;
        aiShieldRoundsLeft = 0;
        resetRoundModifiers();
        playerRoundPowerups.clear();
        aiRoundPowerups.clear();
        playerInventoryPowerups.clear();
        aiInventoryPowerups.clear();
        powerupsUsedThisRound = 0;
    }

    private void resetRoundModifiers() {
        playerTargetOverride = 0;
        aiTargetOverride = 0;
        playerTargetDelta = 0;
        aiTargetDelta = 0;
        playerForesightActive = false;
        aiForesightActive = false;
        if (playerTargetModifiers != null) playerTargetModifiers.clear();
        if (aiTargetModifiers != null) aiTargetModifiers.clear();
        recomputeTargets();
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
        aiDeckTexture = new Texture(Gdx.files.internal("Cards/Deck/Red.png"));
        playerDeckTexture = new Texture(Gdx.files.internal("Cards/Deck/Blue.png"));
        if (Gdx.files.internal("Powerup/UI/pwrupui.png").exists()) {
            powerupPanelBg = new Texture(Gdx.files.internal("Powerup/UI/pwrupui.png"));
        } else {
            powerupPanelBg = null;
        }
        aiDrawVoicesEasy = new Music[] {
            Gdx.audio.newMusic(Gdx.files.internal("Audio/Voice/1Easy/Easy_Draw1.mp3"))
        };
        aiPassVoicesEasy = new Music[] {
            Gdx.audio.newMusic(Gdx.files.internal("Audio/Voice/1Easy/Easy1_Pass1.mp3"))
        };
        aiDrawVoicesMedium = new Music[] {
            Gdx.audio.newMusic(Gdx.files.internal("Audio/Voice/2Medium/Medium_Draw1.mp3")),
            Gdx.audio.newMusic(Gdx.files.internal("Audio/Voice/2Medium/Medium_Draw2.mp3"))
        };
        aiPassVoicesMedium = new Music[] {
            Gdx.audio.newMusic(Gdx.files.internal("Audio/Voice/2Medium/Medium_Pass1.mp3"))
        };
        aiDrawVoicesHard = new Music[] {
            Gdx.audio.newMusic(Gdx.files.internal("Audio/Voice/3Hard/Hard_Draw1.mp3")),
            Gdx.audio.newMusic(Gdx.files.internal("Audio/Voice/3Hard/Hard_Draw2.mp3")),
            Gdx.audio.newMusic(Gdx.files.internal("Audio/Voice/3Hard/Hard_Draw3.mp3"))
        };
        aiPassVoicesHard = new Music[] {
            Gdx.audio.newMusic(Gdx.files.internal("Audio/Voice/3Hard/Hard_Pass1.mp3")),
            Gdx.audio.newMusic(Gdx.files.internal("Audio/Voice/3Hard/Hard_Pass2.mp3"))
        };

        // Init at exit
        updateButtonPositions();

        // neu round
        resetRound();
        turnLabel = "YOUR TURN";
        game.startGameplayMusic();
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
        powerupPanelBounds = new Rectangle(40f, 170f, 560f, 440f);

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
        aiPowerupNotifications.clear();
        showRoundOutcome = false;
        roundOutcomeDraw = false;
        roundOutcomeText = null;
        powerupsUsedThisRound = 0;
        powerupClickBounds.clear();
        powerupClickTypes.clear();
        powerupClickSources.clear();
        resetRoundModifiers();
        tickShieldsForNewRound();
        generateRoundPowerups();

        // Reset deck for new round
        initializeDeck();

        // Draw initial cards (2 for player, 2 for AI)
        drawInitialCards();

        isRoundActive = true;
        gameMessage = "Round " + currentRound + " - Draw a card or pass!";
        isAITurnPending = false;
        aiTurnDelayRemaining = 0f;
        aiPostAction = 0;
        turnLabel = "YOUR TURN";
        aiPlannedShouldDraw = false;
        stopCurrentAIVoice();
    }

    private void tickShieldsForNewRound() {
        if (playerShieldRoundsLeft > 0) {
            playerShield = Math.max(0, playerShield - 1);
            playerShieldRoundsLeft--;
        }
        if (aiShieldRoundsLeft > 0) {
            aiShield = Math.max(0, aiShield - 1);
            aiShieldRoundsLeft--;
        }
    }

    private void generateRoundPowerups() {
        playerRoundPowerups.clear();
        aiRoundPowerups.clear();
        playerRoundPowerups.add(getRandomPowerup());
        playerRoundPowerups.add(getRandomPowerup());
        aiRoundPowerups.add(getRandomPowerup());
        aiRoundPowerups.add(getRandomPowerup());
    }

    private PowerupType getRandomPowerup() {
        PowerupType[] values = PowerupType.values();
        int idx = (int) (Math.random() * values.length);
        if (idx < 0) idx = 0;
        if (idx >= values.length) idx = values.length - 1;
        return values[idx];
    }

    private void recomputeTargets() {
        playerTarget = (playerTargetOverride != 0 ? playerTargetOverride : baseTarget) + playerTargetDelta;
        aiTarget = (aiTargetOverride != 0 ? aiTargetOverride : baseTarget) + aiTargetDelta;
    }

    private void maybeUseAIPowerup() {
        if (powerupsUsedThisRound >= 2) return;
        if (Math.random() > 0.35) return;

        PowerupType powerup = null;
        int source = -1;
        if (aiRoundPowerups.size > 0) {
            powerup = aiRoundPowerups.get(0);
            source = 0;
        } else if (aiInventoryPowerups.size > 0) {
            powerup = aiInventoryPowerups.get(0);
            source = 1;
        }
        if (powerup == null) return;
        tryUsePowerup(false, powerup, source);
    }

    private boolean tryUsePowerup(boolean isPlayerSide, PowerupType powerup, int source) {
        if (!isGameActive || !isRoundActive) {
            if (isPlayerSide) {
                gameMessage = "You can only use powerups during the round.";
            }
            return false;
        }
        if (isPlayerSide && isAITurnPending) {
            gameMessage = "Wait for AI.";
            return false;
        }
        if (powerupsUsedThisRound >= 2) {
            gameMessage = "Max powerups used this round (2).";
            return false;
        }
        if (powerup == null) return false;

        Array<PowerupType> roundList = isPlayerSide ? playerRoundPowerups : aiRoundPowerups;
        Array<PowerupType> invList = isPlayerSide ? playerInventoryPowerups : aiInventoryPowerups;
        if (source == 0) {
            if (!roundList.removeValue(powerup, true)) return false;
        } else {
            if (!invList.removeValue(powerup, true)) return false;
        }

        powerupsUsedThisRound++;
        if (isPlayerSide) {
            game.playPowerupSfx();
        }
        applyPowerup(isPlayerSide, powerup);
        recomputeTargets();
        if (!isPlayerSide) {
            gameMessage += " | AI used " + powerup.getLabel();
            aiPowerupNotifications.add(powerup.getLabel());
        } else {
            gameMessage = "Used " + powerup.getLabel() + ".";
        }
        return true;
    }

    private void applyPowerup(boolean isPlayerSide, PowerupType powerup) {
        switch (powerup) {
            case GO_FOR_17:
                if (isPlayerSide) {
                    playerTargetModifiers.add(new TargetModifier(TARGET_MOD_OVERRIDE, playerTargetOverride));
                    playerTargetOverride = 17;
                } else {
                    aiTargetModifiers.add(new TargetModifier(TARGET_MOD_OVERRIDE, aiTargetOverride));
                    aiTargetOverride = 17;
                }
                break;
            case GO_FOR_24:
                if (isPlayerSide) {
                    playerTargetModifiers.add(new TargetModifier(TARGET_MOD_OVERRIDE, playerTargetOverride));
                    playerTargetOverride = 24;
                } else {
                    aiTargetModifiers.add(new TargetModifier(TARGET_MOD_OVERRIDE, aiTargetOverride));
                    aiTargetOverride = 24;
                }
                break;
            case DOUBLE_DOWN:
                if (isPlayerSide) {
                    playerTargetModifiers.add(new TargetModifier(TARGET_MOD_DELTA, -1));
                    playerTargetDelta -= 1;
                } else {
                    aiTargetModifiers.add(new TargetModifier(TARGET_MOD_DELTA, -1));
                    aiTargetDelta -= 1;
                }
                break;
            case PROTECTION:
                if (isPlayerSide) {
                    playerShield = 2;
                    playerShieldRoundsLeft = 2;
                } else {
                    aiShield = 2;
                    aiShieldRoundsLeft = 2;
                }
                break;
            case REMOVAL:
                removeAllButHidden(isPlayerSide);
                break;
            case RESHUFFLE:
                reshuffleHand(isPlayerSide);
                break;
            case FORESIGHT:
                if (isPlayerSide) playerForesightActive = true;
                else aiForesightActive = true;
                break;
            case SWAP:
                swapOneCard(isPlayerSide);
                break;
            case OVERLOAD:
                forceOpponentDraw(isPlayerSide);
                break;
            case DISCARD:
                discardHighestCard(isPlayerSide);
                break;
            case TOSS:
                tossLastDebuff(isPlayerSide);
                break;
            case CLEAR:
                clearRoundEffectsForSide(isPlayerSide);
                break;
        }
    }

    private void tossLastDebuff(boolean isPlayerSide) {
        Array<TargetModifier> mods = isPlayerSide ? playerTargetModifiers : aiTargetModifiers;
        if (mods == null || mods.size == 0) return;
        TargetModifier last = mods.pop();
        if (last.kind == TARGET_MOD_OVERRIDE) {
            if (isPlayerSide) {
                playerTargetOverride = last.value;
            } else {
                aiTargetOverride = last.value;
            }
        } else if (last.kind == TARGET_MOD_DELTA) {
            if (isPlayerSide) {
                playerTargetDelta -= last.value;
            } else {
                aiTargetDelta -= last.value;
            }
        }
    }

    private void clearRoundEffectsForSide(boolean isPlayerSide) {
        if (isPlayerSide) {
            playerTargetOverride = 0;
            playerTargetDelta = 0;
            playerForesightActive = false;
            if (playerTargetModifiers != null) playerTargetModifiers.clear();
        } else {
            aiTargetOverride = 0;
            aiTargetDelta = 0;
            aiForesightActive = false;
            if (aiTargetModifiers != null) aiTargetModifiers.clear();
        }
    }

    private void removeAllButHidden(boolean isPlayerSide) {
        Array<Integer> cards = isPlayerSide ? playerCards : aiCards;
        if (cards.size <= 1) return;
        int first = cards.get(0);
        cards.clear();
        cards.add(first);
        insertionSort(cards, isPlayerSide ? playerSortedCards : aiSortedCards);
        calculateScores();
    }

    private void reshuffleHand(boolean isPlayerSide) {
        Array<Integer> cards = isPlayerSide ? playerCards : aiCards;
        if (deck == null || cards == null) return;
        if (cards.size == 0) return;
        int handSize = cards.size;
        for (int i = 0; i < handSize; i++) {
            deck.add(cards.get(i));
        }
        shuffleDeck();
        for (int i = 0; i < handSize; i++) {
            if (deck.isEmpty()) break;
            cards.set(i, deck.pop());
        }
        insertionSort(cards, isPlayerSide ? playerSortedCards : aiSortedCards);
        calculateScores();
    }

    private void swapOneCard(boolean isPlayerSide) {
        Array<Integer> cards = isPlayerSide ? playerCards : aiCards;
        if (deck == null || cards == null) return;
        if (cards.size == 0) return;
        if (deck.isEmpty()) return;
        int startIndex = cards.size > 1 ? 1 : 0;
        int index = startIndex;
        int range = cards.size - startIndex;
        if (range > 1) {
            index = startIndex + (int) (Math.random() * range);
        }
        int newCard = deck.pop();
        int oldCard = cards.get(index);
        cards.set(index, newCard);
        deck.add(oldCard);
        shuffleDeck();
        insertionSort(cards, isPlayerSide ? playerSortedCards : aiSortedCards);
        calculateScores();
    }

    private void forceOpponentDraw(boolean isPlayerSide) {
        if (deck.isEmpty()) return;
        boolean opponentIsPlayer = !isPlayerSide;
        int card = deck.pop();
        Array<Integer> cards = opponentIsPlayer ? playerCards : aiCards;
        cards.add(card);
        insertionSort(cards, opponentIsPlayer ? playerSortedCards : aiSortedCards);
        calculateScores();
    }

    private void discardHighestCard(boolean isPlayerSide) {
        Array<Integer> cards = isPlayerSide ? playerCards : aiCards;
        if (cards.size == 0) return;
        int max = cards.get(0);
        for (int i = 1; i < cards.size; i++) {
            max = Math.max(max, cards.get(i));
        }
        for (int i = 0; i < cards.size; i++) {
            if (cards.get(i) == max) {
                cards.removeIndex(i);
                break;
            }
        }
        insertionSort(cards, isPlayerSide ? playerSortedCards : aiSortedCards);
        calculateScores();
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
        roundOutcomeDraw = false;
        roundOutcomeText = null;
        isMatchEndOpen = false;
        isLevelSelectOpen = false;
        resetRound();
        isGameActive = true;
        game.startGameplayMusic();
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
        drawShieldBar(barX, aiHealthY - 10f, aiShield, maxShield);

        drawHealthBar(barX, playerHealthY, playerLives, maxPlayerLives);
        drawShieldBar(barX, playerHealthY - 10f, playerShield, maxShield);

        if (aiDeckTexture != null) {
            float deckH = 140f;
            float deckW = 170f;
            drawRoundedTexture(aiDeckTexture, 80f, 480f, deckW, deckH, 12f);
        }
        if (playerDeckTexture != null) {
            float deckH = 140f;
            float deckW = 170f;
            drawRoundedTexture(playerDeckTexture, 1010f, 115f, deckW, deckH, 12f);
        }

        float turnPrevScaleX = game.titleFont.getData().scaleX;
        float turnPrevScaleY = game.titleFont.getData().scaleY;
        game.titleFont.getData().setScale(layoutScale * 0.42f);
        game.titleFont.setColor(Color.WHITE);
        String tl = turnLabel == null ? "" : turnLabel;
        layout.setText(game.titleFont, tl);
        game.titleFont.draw(batch, tl, sx(160f) - layout.width / 2f, sy(680f));
        game.titleFont.getData().setScale(turnPrevScaleX, turnPrevScaleY);

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

        String playerScoreText = playerScore + "/" + playerTarget;
        String aiScoreText = isRoundActive ? "? + " + getAIRevealedScore() + "/" + aiTarget : aiScore + "/" + aiTarget;
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
            batch.setColor(1f, 1f, 1f, 1f);
            if (powerupPanelBg != null) {
                batch.draw(powerupPanelBg, sx(powerupPanelBounds.x), sy(powerupPanelBounds.y), ss(powerupPanelBounds.width), ss(powerupPanelBounds.height));
            } else {
                batch.setColor(0.15f, 0.15f, 0.15f, 0.95f);
                batch.draw(solidPixel != null ? solidPixel : game.backgroundRectangle, sx(powerupPanelBounds.x), sy(powerupPanelBounds.y), ss(powerupPanelBounds.width), ss(powerupPanelBounds.height));
                batch.setColor(1f, 1f, 1f, 1f);
            }
            batch.setColor(Color.WHITE);

            powerupClickBounds.clear();
            powerupClickTypes.clear();
            powerupClickSources.clear();

            float prevBodyScaleX = game.bodyFont.getData().scaleX;
            float prevBodyScaleY = game.bodyFont.getData().scaleY;
            game.bodyFont.getData().setScale(layoutScale * 0.78f);
            game.bodyFont.setColor(Color.WHITE);
            float panelX = powerupPanelBounds.x;
            float panelY = powerupPanelBounds.y;
            float panelW = powerupPanelBounds.width;
            float panelH = powerupPanelBounds.height;
            float pad = 18f;
            float headerY = panelY + panelH - 26f;

            game.bodyFont.setColor(Color.GOLD);
            String usedText = "Used: " + powerupsUsedThisRound + "/2";
            layout.setText(game.bodyFont, usedText);
            game.bodyFont.draw(batch, usedText, sx(panelX + panelW - pad - layout.width), sy(headerY));

            String roundHeader = "This Round:";
            game.bodyFont.setColor(Color.WHITE);
            game.bodyFont.draw(batch, roundHeader, sx(panelX + pad), sy(headerY));
            String roundList = formatPowerupList(playerRoundPowerups, 2);
            game.bodyFont.setColor(Color.YELLOW);
            game.bodyFont.draw(batch, roundList, sx(panelX + pad), sy(headerY - 20f));

            String infoHeader = "Info:";
            game.bodyFont.setColor(Color.WHITE);
            layout.setText(game.bodyFont, infoHeader);
            game.bodyFont.draw(batch, infoHeader, sx(panelX + panelW - pad - layout.width), sy(headerY - 20f));
            String infoText = getPlayerPowerupInfoText();
            game.bodyFont.setColor(Color.CYAN);
            layout.setText(game.bodyFont, infoText);
            game.bodyFont.draw(batch, infoText, sx(panelX + panelW - pad - layout.width), sy(headerY - 40f));

            float gridTop = headerY - 70f;
            float gridLeft = panelX + pad;
            float gridW = panelW - pad * 2f;
            float gridH = panelH - 120f;
            int cols = 3;
            int rows = 4;
            float gridGap = 10f;
            float cellW = (gridW - gridGap * (cols - 1)) / cols;
            float cellH = (gridH - gridGap * (rows - 1)) / rows;

            PowerupType[] all = PowerupType.values();
            for (int i = 0; i < all.length; i++) {
                int r = i / cols;
                int c = i % cols;
                float cx = gridLeft + c * (cellW + gridGap);
                float cy = gridTop - r * (cellH + gridGap) - cellH;
                PowerupType p = all[i];
                int roundCount = countPowerup(playerRoundPowerups, p);
                int invCount = countPowerup(playerInventoryPowerups, p);
                int total = roundCount + invCount;

                if (total > 0) {
                    batch.setColor(1f, 0.85f, 0.2f, 0.95f);
                } else {
                    batch.setColor(0.2f, 0.2f, 0.2f, 0.85f);
                }
                batch.draw(solidPixel != null ? solidPixel : game.backgroundRectangle, sx(cx), sy(cy), ss(cellW), ss(cellH));
                batch.setColor(Color.WHITE);

                String label = p.getLabel();
                float textX = cx + 10f;
                float textY = cy + cellH - 12f;
                game.bodyFont.setColor(total > 0 ? Color.BLACK : Color.LIGHT_GRAY);
                game.bodyFont.draw(batch, label, sx(textX), sy(textY));

                String countText = "x" + total;
                layout.setText(game.bodyFont, countText);
                game.bodyFont.draw(batch, countText, sx(cx + cellW - 10f - layout.width), sy(cy + 18f));
                game.bodyFont.setColor(Color.WHITE);

                if (total > 0) {
                    powerupClickBounds.add(new Rectangle(cx, cy, cellW, cellH));
                    powerupClickTypes.add(p);
                    powerupClickSources.add(roundCount > 0 ? 0 : 1);
                }
            }

            game.bodyFont.getData().setScale(prevBodyScaleX, prevBodyScaleY);
        }

        if (showRoundOutcome && !isRoundActive && isGameActive) {
            if (roundOutcomeDraw) {
                batch.setColor(0f, 0f, 0f, 0.30f);
            } else if (roundOutcomeWin) {
                batch.setColor(0f, 0f, 0f, 0.30f);
            } else {
                batch.setColor(1f, 0f, 0f, 0.30f);
            }
            batch.draw(solidPixel != null ? solidPixel : game.backgroundRectangle, sx(0), sy(0), ss(REF_W), ss(REF_H));
            batch.setColor(Color.WHITE);

            float prevScaleX = game.titleFont.getData().scaleX;
            float prevScaleY = game.titleFont.getData().scaleY;
            game.titleFont.getData().setScale(layoutScale * 0.85f);
            if (roundOutcomeDraw) {
                game.titleFont.setColor(Color.LIGHT_GRAY);
            } else {
                game.titleFont.setColor(Color.WHITE);
            }
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
        if (aiPowerupNotifications != null && aiPowerupNotifications.size > 0) {
            float prevScaleX = game.bodyFont.getData().scaleX;
            float prevScaleY = game.bodyFont.getData().scaleY;
            game.bodyFont.getData().setScale(layoutScale * 0.8f);
            game.bodyFont.setColor(Color.CYAN);
            float x = 1120f;
            float y = 615f;
            layout.setText(game.bodyFont, "AI USED:");
            game.bodyFont.draw(batch, "AI USED:", sx(x), sy(y));
            y -= 22f;
            int maxLines = 6;
            for (int i = 0; i < aiPowerupNotifications.size && i < maxLines; i++) {
                String t = aiPowerupNotifications.get(i);
                layout.setText(game.bodyFont, t);
                game.bodyFont.draw(batch, t, sx(x), sy(y));
                y -= 20f;
            }
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
            batch.setColor(0f, 0f, 0f, 0.70f);
            batch.draw(solidPixel, sx(0), sy(0), ss(REF_W), ss(REF_H));

            float matchPrevTitleScaleX = game.titleFont.getData().scaleX;
            float matchPrevTitleScaleY = game.titleFont.getData().scaleY;
            game.titleFont.getData().setScale(layoutScale * 0.95f);
            String title = matchWon ? "YOU WON!" : "YOU LOSE!";
            game.titleFont.setColor(matchWon ? Color.YELLOW : Color.RED);
            layout.setText(game.titleFont, title);
            float matchCenterX = REF_W / 2f;
            float matchCenterY = REF_H / 2f + 100f;
            game.titleFont.draw(batch, title, sx(matchCenterX) - layout.width / 2f, sy(matchCenterY));
            game.titleFont.getData().setScale(matchPrevTitleScaleX, matchPrevTitleScaleY);

            float prevBodyScaleX = game.bodyFont.getData().scaleX;
            float prevBodyScaleY = game.bodyFont.getData().scaleY;
            game.bodyFont.getData().setScale(layoutScale * 0.9f);

            batch.setColor(0.15f, 0.15f, 0.15f, 0.92f);
            batch.draw(solidPixel, sx(matchEndPlayAgainBounds.x), sy(matchEndPlayAgainBounds.y), ss(matchEndPlayAgainBounds.width), ss(matchEndPlayAgainBounds.height));
            batch.draw(solidPixel, sx(matchEndHomeBounds.x), sy(matchEndHomeBounds.y), ss(matchEndHomeBounds.width), ss(matchEndHomeBounds.height));
            batch.setColor(Color.WHITE);

            String playAgainText = "Play Again";
            String menuText = "Main Menu";
            game.bodyFont.setColor(Color.WHITE);
            layout.setText(game.bodyFont, playAgainText);
            game.bodyFont.draw(batch, playAgainText,
                sx(matchEndPlayAgainBounds.x + (matchEndPlayAgainBounds.width - layout.width) / 2f),
                sy(matchEndPlayAgainBounds.y + (matchEndPlayAgainBounds.height + layout.height) / 2f));
            layout.setText(game.bodyFont, menuText);
            game.bodyFont.draw(batch, menuText,
                sx(matchEndHomeBounds.x + (matchEndHomeBounds.width - layout.width) / 2f),
                sy(matchEndHomeBounds.y + (matchEndHomeBounds.height + layout.height) / 2f));

            batch.setColor(Color.WHITE);
        }

        batch.end();

        updateAITurn(delta);
        handleGameInput();

        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        handleMouseInput(mouseX, mouseY);
    }

    private void handleGameInput() {
        if (isMatchEndOpen) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
                returnToMenu();
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
            if (!isAITurnPending) {
                if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
                    drawCardForPlayer();
                }

                if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.P)) {
                    passTurn();
                }
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
                    game.playButtonSfx();
                    isMatchEndOpen = false;
                    isLevelSelectOpen = true;
                    return;
                }
                if (matchEndHomeBounds.contains(refMouseX, refMouseY)) {
                    game.playButtonSfx();
                    isMatchEndOpen = false;
                    returnToMenu();
                    return;
                }
                return;
            }
            if (isLevelSelectOpen) {
                if (levelEasyBounds.contains(refMouseX, refMouseY)) {
                    game.playButtonSfx();
                    game.difficulty = 0;
                    resetGame();
                    isLevelSelectOpen = false;
                    isMatchEndOpen = false;
                    return;
                }
                if (levelMediumBounds.contains(refMouseX, refMouseY)) {
                    game.playButtonSfx();
                    game.difficulty = 1;
                    resetGame();
                    isLevelSelectOpen = false;
                    isMatchEndOpen = false;
                    return;
                }
                if (levelHardBounds.contains(refMouseX, refMouseY)) {
                    game.playButtonSfx();
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
                    game.playButtonSfx();
                    isQuitConfirmOpen = false;
                    isOptionsOpen = false;
                    returnToMenu();
                    return;
                }
                if (quitNoBounds.contains(refMouseX, refMouseY) || !quitConfirmPanelBounds.contains(refMouseX, refMouseY)) {
                    game.playButtonSfx();
                    isQuitConfirmOpen = false;
                    return;
                }
                return;
            }
            if (isOptionsOpen) {
                if (optionsRestartBounds.contains(refMouseX, refMouseY)) {
                    game.playButtonSfx();
                    isOptionsOpen = false;
                    isQuitConfirmOpen = false;
                    isPowerupPanelOpen = false;
                    showRoundOutcome = false;
                    roundOutcomeText = null;
                    resetGame();
                    return;
                }
                if (optionsChangeLevelBounds.contains(refMouseX, refMouseY)) {
                    game.playButtonSfx();
                    isOptionsOpen = false;
                    isQuitConfirmOpen = false;
                    isPowerupPanelOpen = false;
                    isMatchEndOpen = false;
                    isLevelSelectOpen = true;
                    return;
                }
                if (optionsHomeBounds.contains(refMouseX, refMouseY)) {
                    game.playButtonSfx();
                    isQuitConfirmOpen = true;
                    return;
                }
                if (!optionsPanelBounds.contains(refMouseX, refMouseY)) {
                    isOptionsOpen = false;
                }
                return;
            }
            if (isPowerupPanelOpen) {
                for (int i = 0; i < powerupClickBounds.size; i++) {
                    Rectangle bounds = powerupClickBounds.get(i);
                    if (bounds.contains(refMouseX, refMouseY)) {
                        game.playButtonSfx();
                        tryUsePowerup(true, powerupClickTypes.get(i), powerupClickSources.get(i));
                        return;
                    }
                }
                if (!powerupPanelBounds.contains(refMouseX, refMouseY)) {
                    isPowerupPanelOpen = false;
                }
                return;
            }
            if (optionsButtonBounds != null && optionsButtonBounds.contains(refMouseX, refMouseY)) {
                game.playButtonSfx();
                isOptionsOpen = !isOptionsOpen;
                return;
            }
            if (powerupButton != null && powerupButton.contains(refMouseX, refMouseY)) {
                game.playButtonSfx();
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
        playerForesightActive = false;
        game.playCardSfx();

        //INSERTION SORT PALDO DIBA
        insertionSort(playerCards, playerSortedCards);


        calculateScores();


        gameMessage = "You drew card: " + card + " (Total: " + playerScore + ")";
        scheduleAITurn(1);
    }

    private void executeAITurn(boolean shouldDraw) {
        maybeUseAIPowerup();
        if (deck.isEmpty()) {
            gameMessage += " | AI passed";
            return;
        }

        if (shouldDraw) {
            int card = deck.pop();
            aiCards.add(card);
            aiForesightActive = false;


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

    private void scheduleAITurn(int postAction) {
        isAITurnPending = true;
        aiPostAction = postAction;
        turnLabel = "AI TURN";
        aiTurnDelayRemaining = 0f;
        aiPlannedShouldDraw = computeAIShouldDraw();
        startAIVoice(aiPlannedShouldDraw);
    }

    private boolean computeAIShouldDraw() {
        if (deck.isEmpty()) return false;
        if (aiScore < 15) {
            return true;
        } else if (aiScore < 18) {
            return Math.random() > 0.3;
        } else {
            return Math.random() > 0.7;
        }
    }

    private void startAIVoice(boolean isDraw) {
        stopCurrentAIVoice();
        Music[] voices;
        switch (game.difficulty) {
            case 0:
                voices = isDraw ? aiDrawVoicesEasy : aiPassVoicesEasy;
                break;
            case 2:
                voices = isDraw ? aiDrawVoicesHard : aiPassVoicesHard;
                break;
            case 1:
            default:
                voices = isDraw ? aiDrawVoicesMedium : aiPassVoicesMedium;
                break;
        }

        currentAIVoice = pickNonRepeatingVoice(voices, isDraw);
        if (currentAIVoice != null) {
            currentAIVoice.setVolume(game.getVoiceVolume());
            currentAIVoice.play();
        } else {
            aiTurnDelayRemaining = aiTurnDelaySeconds;
        }
    }

    private Music pickNonRepeatingVoice(Music[] voices, boolean isDraw) {
        if (voices == null || voices.length == 0) return null;
        int idx = (int) (Math.random() * voices.length);
        if (voices.length > 1) {
            int last = isDraw ? lastDrawIdx : lastPassIdx;
            if (idx == last) {
                idx = (idx + 1) % voices.length;
            }
        }
        if (isDraw) {
            lastDrawIdx = idx;
        } else {
            lastPassIdx = idx;
        }
        Music m = voices[idx];
        if (m != null) {
            m.stop();
        }
        return m;
    }

    private void stopCurrentAIVoice() {
        if (currentAIVoice != null) {
            currentAIVoice.stop();
            currentAIVoice = null;
        }
    }

    private void passTurn() {
        if (!isRoundActive || !isGameActive) return;


        gameMessage = "You passed.";
        scheduleAITurn(2);
    }

    private void checkRoundEnd() {

        boolean playerBust = playerScore > playerTarget;
        boolean aiBust = aiScore > aiTarget;

        if (playerBust || aiBust || deck.isEmpty()) {
            isRoundActive = false;
            endRound();
        }
    }

    private void endRound() {
        int prevPlayerLives = playerLives;
        int prevAiLives = aiLives;
        String resultMessage;

        storeUnusedRoundPowerups();

        int playerDiff = Math.abs(playerTarget - playerScore);
        int aiDiff = Math.abs(aiTarget - aiScore);
        boolean isDraw = playerScore == aiScore || playerDiff == aiDiff;

        if (isDraw) {
            resultMessage = "Draw! No lives lost.";
        } else if (playerDiff < aiDiff) {
            damageAI();
            playerWins++;
            resultMessage = "You win! AI loses a life.";
        } else {
            damagePlayer();
            aiWins++;
            resultMessage = "AI wins! You lose a life.";
        }

        // Add card info to message
        resultMessage += " Cards: You [" + formatCards(playerSortedCards) + "] vs AI [" + formatCards(aiSortedCards) + "]";
        gameMessage = resultMessage + " Press SPACE for next round.";

        if (isDraw) {
            showRoundOutcome = true;
            roundOutcomeDraw = true;
            roundOutcomeWin = false;
            roundOutcomeText = "DRAW";
        } else if (playerLives < prevPlayerLives) {
            showRoundOutcome = true;
            roundOutcomeDraw = false;
            roundOutcomeWin = false;
            roundOutcomeText = "-1HP you loss";
        } else if (aiLives < prevAiLives) {
            showRoundOutcome = true;
            roundOutcomeDraw = false;
            roundOutcomeWin = true;
            roundOutcomeText = "You survived";
        } else {
            showRoundOutcome = false;
            roundOutcomeDraw = false;
            roundOutcomeText = null;
        }

        // Check for game over
        if (playerLives <= 0 || aiLives <= 0) {
            endGame();
        }
    }

    private void updateAITurn(float delta) {
        if (!isGameActive || !isRoundActive) return;
        if (!isAITurnPending) return;
        if (currentAIVoice != null && currentAIVoice.isPlaying()) return;
        if (aiTurnDelayRemaining > 0f) {
            aiTurnDelayRemaining -= delta;
            if (aiTurnDelayRemaining > 0f) return;
        }
        executeAITurn(aiPlannedShouldDraw);
        stopCurrentAIVoice();
        isAITurnPending = false;
        turnLabel = "YOUR TURN";
        if (aiPostAction == 1) {
            checkRoundEnd();
        } else if (aiPostAction == 2) {
            isRoundActive = false;
            endRound();
        }
        aiPostAction = 0;
    }
    private void damagePlayer() {
        if (playerShield > 0) {
            playerShield = Math.max(0, playerShield - 1);
            return;
        }
        playerLives--;
    }

    private void damageAI() {
        if (aiShield > 0) {
            aiShield = Math.max(0, aiShield - 1);
            return;
        }
        aiLives--;
    }

    private void storeUnusedRoundPowerups() {
        for (PowerupType powerup : playerRoundPowerups) {
            playerInventoryPowerups.add(powerup);
        }
        for (PowerupType powerup : aiRoundPowerups) {
            aiInventoryPowerups.add(powerup);
        }
        playerRoundPowerups.clear();
        aiRoundPowerups.clear();
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

        matchWon = playerLives > 0;
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
        game.playMenuMusic();
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
        if (aiDeckTexture != null) {
            aiDeckTexture.dispose();
            aiDeckTexture = null;
        }
        if (playerDeckTexture != null) {
            playerDeckTexture.dispose();
            playerDeckTexture = null;
        }
        if (cardShader != null) {
            cardShader.dispose();
            cardShader = null;
        }
        if (powerupPanelBg != null) {
            powerupPanelBg.dispose();
            powerupPanelBg = null;
        }
        disposeMusic(aiDrawVoicesEasy);
        disposeMusic(aiPassVoicesEasy);
        disposeMusic(aiDrawVoicesMedium);
        disposeMusic(aiPassVoicesMedium);
        disposeMusic(aiDrawVoicesHard);
        disposeMusic(aiPassVoicesHard);
    }

    private void disposeMusic(Music[] musics) {
        if (musics == null) return;
        for (Music m : musics) {
            if (m != null) m.dispose();
        }
    }

    private int countPowerup(Array<PowerupType> list, PowerupType type) {
        if (list == null || type == null) return 0;
        int count = 0;
        for (int i = 0; i < list.size; i++) {
            if (list.get(i) == type) count++;
        }
        return count;
    }

    private String formatPowerupList(Array<PowerupType> list, int maxItems) {
        if (list == null || list.size == 0) return "-";
        StringBuilder sb = new StringBuilder();
        int lim = Math.min(maxItems, list.size);
        for (int i = 0; i < lim; i++) {
            if (i > 0) sb.append(" | ");
            sb.append(list.get(i).getLabel());
        }
        return sb.toString();
    }

    private String getPlayerPowerupInfoText() {
        if (playerForesightActive) {
            if (deck != null && deck.size > 0) {
                return "Next: " + deck.peek();
            }
            return "-";
        }
        return "-";
    }
}
