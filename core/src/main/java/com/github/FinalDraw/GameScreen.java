package com.github.FinalDraw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class GameScreen implements Screen {
    private final Core game;
    private SpriteBatch batch;
    private GlyphLayout layout;

    //Vars
    private Rectangle exitButton;
    private boolean isGameActive;
    private boolean isRoundActive;

    //Buhay and shi
    private int playerLives;
    private int aiLives;
    private int maxPlayerLives;
    private int maxAILives;

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

    //heart display REPLACE LATER HA! Placeholder!!!
    private TextureRegion heartFull;
    private TextureRegion heartEmpty;
    private float heartSize = 32f;
    private float heartSpacing = 40f;

    //track mo rounds
    private int currentRound;
    private int playerWins;
    private int aiWins;

    //card display PLACEHOLDER REPLACE DENG HA!
    private float cardSpacing = 60f;
    private float cardWidth = 50f;
    private float cardHeight = 70f;

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

        switch (game.difficulty) {
            case 0: // Easy
                maxPlayerLives = 5;
                maxAILives = 5;
                difficultyName = "Easy";
                break;
            case 1: // Medium (default)
                maxPlayerLives = 5;
                maxAILives = 7;
                difficultyName = "Medium";
                break;
            case 2: // Hard
                maxPlayerLives = 5;
                maxAILives = 11;
                difficultyName = "Hard";
                break;
            default:
                maxPlayerLives = 5;
                maxAILives = 7;
                difficultyName = "Medium";
                break;
        }

        playerLives = maxPlayerLives;
        aiLives = maxAILives;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        layout = new GlyphLayout();

        //heart textures REPLACE!
        heartFull = new TextureRegion(game.backgroundRectangle);
        heartEmpty = new TextureRegion(game.backgroundRectangle);

        // Init at exit
        updateButtonPositions();

        // neu round
        resetRound();
    }

    private void updateButtonPositions() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();


        exitButton = new Rectangle(screenWidth - 150, screenHeight - 80, 130, 50);
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
        // Sum all AI cards except the first one (which is hidden during round)
        for (int i = 1; i < aiCards.size; i++) {
            revealedScore += aiCards.get(i);
        }
        return revealedScore;
    }

    private void resetGame() {
        // Reset lives based on current difficulty
        setupDifficulty();
        currentRound = 1;
        playerWins = 0;
        aiWins = 0;
        resetRound();
        isGameActive = true;
    }

    private void drawHearts(float x, float y, int lives, int maxLives, boolean isPlayerSide) {
        batch.setColor(Color.WHITE);

        // Calculate adjusted spacing based on number of hearts
        float adjustedSpacing = heartSpacing;
        if (maxLives > 10) {
            adjustedSpacing = 30f; // Closer spacing for 11 hearts
        } else if (maxLives > 7) {
            adjustedSpacing = 35f; // Slightly closer for 7-10 hearts
        }

        for (int i = 0; i < maxLives; i++) {
            float heartX;
            if (isPlayerSide) {
                heartX = x + i * adjustedSpacing;
            } else {
                // For AI, draw from right to left
                heartX = x - i * adjustedSpacing;
            }

            if (i < lives) {
                // Full heart (red)
                batch.setColor(Color.RED);
                batch.draw(heartFull, heartX, y, heartSize, heartSize);

                // Add white border/outline
                batch.setColor(Color.WHITE);
                batch.draw(game.backgroundRectangle,
                    heartX - 1, y - 1,
                    heartSize + 2, heartSize + 2);
            } else {
                // Empty heart (gray with red outline)
                batch.setColor(0.3f, 0.3f, 0.3f, 1f); // Dark gray
                batch.draw(heartEmpty, heartX, y, heartSize, heartSize);

                // Red outline for lost hearts
                batch.setColor(Color.RED);
                batch.draw(game.backgroundRectangle,
                    heartX - 1, y - 1,
                    heartSize + 2, heartSize + 2);
            }
        }

        batch.setColor(Color.WHITE);
    }

    private void drawCard(float x, float y, int cardValue, boolean isPlayer, boolean isHidden) {

        batch.setColor(isPlayer ? Color.GREEN : Color.RED);
        batch.draw(game.backgroundRectangle, x, y, cardWidth, cardHeight);


        batch.setColor(Color.WHITE);
        batch.draw(game.backgroundRectangle, x - 2, y - 2, cardWidth + 4, cardHeight + 4);


        if (isHidden) {
            game.bodyFont.setColor(Color.BLACK);
            game.bodyFont.draw(batch, "?",
                x + cardWidth/2 - 5, y + cardHeight/2 + 5);
        } else {
            game.bodyFont.setColor(Color.WHITE);
            String cardText = String.valueOf(cardValue);
            layout.setText(game.bodyFont, cardText);
            game.bodyFont.draw(batch, cardText,
                x + cardWidth/2 - layout.width/2,
                y + cardHeight/2 + layout.height/2);
        }
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        batch.draw(game.backgroundStatic, 0, 0,
            Gdx.graphics.getWidth(), Gdx.graphics.getHeight());


        batch.draw(game.shadow, 0, 0,
            Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Draw game title with round number and difficulty
        game.titleFont.setColor(Color.WHITE);
        String title = "ROUND " + currentRound + " - " + difficultyName;
        layout.setText(game.titleFont, title);
        game.titleFont.draw(batch, title,
            (Gdx.graphics.getWidth() - layout.width) / 2,
            Gdx.graphics.getHeight() - 70);


        float livesY = Gdx.graphics.getHeight() - 120;
        float playerLivesX = 50;
        float aiLivesX = Gdx.graphics.getWidth() - 50; // Start from right edge


        game.bodyFont.setColor(Color.GREEN);
        String playerLabel = "PLAYER LIVES";
        layout.setText(game.bodyFont, playerLabel);
        game.bodyFont.draw(batch, playerLabel,
            playerLivesX,
            livesY + heartSize + 20);

        // Draw player hearts (5 max) Mahirap yung 5 lowkey hahaha.
        drawHearts(playerLivesX, livesY, playerLives, maxPlayerLives, true);

        // Draw player lives count
        String playerCount = playerLives + "/" + maxPlayerLives;
        game.bodyFont.setColor(Color.GREEN);
        layout.setText(game.bodyFont, playerCount);
        game.bodyFont.draw(batch, playerCount,
            playerLivesX + (maxPlayerLives * heartSpacing) / 2 - layout.width / 2,
            livesY - 10);

        // Draw AI lives on right side
        game.bodyFont.setColor(Color.RED);
        String aiLabel = "AI LIVES";
        layout.setText(game.bodyFont, aiLabel);
        game.bodyFont.draw(batch, aiLabel,
            aiLivesX - (maxAILives * heartSpacing) / 2 - layout.width / 2,
            livesY + heartSize + 20);

        drawHearts(aiLivesX, livesY, aiLives, maxAILives, false);

        // Draw AI lives count
        String aiCount = aiLives + "/" + maxAILives;
        game.bodyFont.setColor(Color.RED);
        layout.setText(game.bodyFont, aiCount);
        game.bodyFont.draw(batch, aiCount,
            aiLivesX - (maxAILives * heartSpacing) / 2 - layout.width / 2,
            livesY - 10);


        float messageY = Gdx.graphics.getHeight() - 240;
        game.bodyFont.setColor(Color.WHITE);
        layout.setText(game.bodyFont, gameMessage);
        game.bodyFont.draw(batch, gameMessage,
            (Gdx.graphics.getWidth() - layout.width) / 2,
            messageY);

        // Draw game instructions
        float instructionY = messageY - 25;
        game.bodyFont.setColor(Color.YELLOW);
        String instruction = isRoundActive ?
            "SPACE: Draw Card | P: Pass" :
            "SPACE: Next Round";
        layout.setText(game.bodyFont, instruction);
        game.bodyFont.draw(batch, instruction,
            (Gdx.graphics.getWidth() - layout.width) / 2,
            instructionY);

        // Draw cards section
        float cardsY = Gdx.graphics.getHeight() / 2 - 40;
        float centerX = Gdx.graphics.getWidth() / 2;


        float playerCardsX = centerX - 250;
        game.titleFont.setColor(Color.GREEN);
        String playerCardsLabel = "PLAYER CARDS";
        layout.setText(game.titleFont, playerCardsLabel);
        game.titleFont.draw(batch, playerCardsLabel,
            playerCardsX + (playerSortedCards.size * cardSpacing) / 2 - layout.width / 2,
            cardsY + 100);


        for (int i = 0; i < playerSortedCards.size; i++) {
            boolean isHidden = (i == 0 && isRoundActive); // First card hidden during round
            drawCard(playerCardsX + i * cardSpacing, cardsY,
                playerSortedCards.get(i), true, isHidden);
        }


        String playerText = "Score: " + playerScore;
        game.titleFont.setColor(Color.GREEN);
        layout.setText(game.titleFont, playerText);
        game.titleFont.draw(batch, playerText,
            playerCardsX + (playerSortedCards.size * cardSpacing) / 2 - layout.width / 2,
            cardsY - 40);


        float aiCardsX = centerX + 50;
        game.titleFont.setColor(Color.RED);
        String aiCardsLabel = "AI CARDS";
        layout.setText(game.titleFont, aiCardsLabel);
        game.titleFont.draw(batch, aiCardsLabel,
            aiCardsX + (aiSortedCards.size * cardSpacing) / 2 - layout.width / 2,
            cardsY + 100);


        for (int i = 0; i < aiSortedCards.size; i++) {
            boolean isHidden = (i == 0 && isRoundActive); // First card hidden during round
            drawCard(aiCardsX + i * cardSpacing, cardsY,
                aiSortedCards.get(i), false, isHidden);
        }


        String aiText;
        if (isRoundActive) {

            int revealedScore = getAIRevealedScore();
            aiText = "Score: ? + " + revealedScore;
        } else {

            aiText = "Score: " + aiScore;
        }

        game.titleFont.setColor(Color.RED);
        layout.setText(game.titleFont, aiText);
        game.titleFont.draw(batch, aiText,
            aiCardsX + (aiSortedCards.size * cardSpacing) / 2 - layout.width / 2,
            cardsY - 40);


        float winCounterY = cardsY - 100;
        game.bodyFont.setColor(Color.LIGHT_GRAY);
        String winsText = "Wins: " + playerWins + " - " + aiWins;
        layout.setText(game.bodyFont, winsText);
        game.bodyFont.draw(batch, winsText,
            centerX - layout.width / 2, winCounterY);


        game.bodyFont.setColor(Color.LIGHT_GRAY);
        String deckText = "Cards in deck: " + deck.size;
        layout.setText(game.bodyFont, deckText);
        game.bodyFont.draw(batch, deckText,
            centerX - layout.width / 2, winCounterY - 30);


        if (!isGameActive) {
            float statusY = winCounterY - 80;
            game.titleFont.setColor(Color.GOLD);
            String status = playerLives <= 0 ? "GAME OVER - AI WINS!" : "VICTORY - YOU WIN!";
            layout.setText(game.titleFont, status);
            game.titleFont.draw(batch, status,
                (Gdx.graphics.getWidth() - layout.width) / 2,
                statusY);
        }


        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();


        drawExitButton(mouseX, mouseY);


        String controls = "ESC: Main Menu";
        game.bodyFont.setColor(Color.LIGHT_GRAY);
        layout.setText(game.bodyFont, controls);
        game.bodyFont.draw(batch, controls,
            (Gdx.graphics.getWidth() - layout.width) / 2,
            50);

        batch.end();


        handleGameInput();


        handleMouseInput(mouseX, mouseY);
    }

    private void drawExitButton(float mouseX, float mouseY) {
        String buttonText = "Exit to Menu";
        boolean isHovered = exitButton.contains(mouseX, mouseY);


        batch.setColor(0.2f, 0.2f, 0.2f, 0.8f);
        batch.draw(game.backgroundRectangle,
            exitButton.x, exitButton.y,
            exitButton.width, exitButton.height);


        if (isHovered) {

            game.bodyFont.setColor(0, 0, 0, 0.5f);
            layout.setText(game.bodyFont, buttonText);
            game.bodyFont.draw(batch, buttonText,
                exitButton.x + (exitButton.width - layout.width) / 2 + 2,
                exitButton.y + (exitButton.height + layout.height) / 2 - 2);


            game.bodyFont.setColor(Color.YELLOW);
            game.bodyFont.draw(batch, buttonText,
                exitButton.x + (exitButton.width - layout.width) / 2,
                exitButton.y + (exitButton.height + layout.height) / 2);
        } else {

            game.bodyFont.setColor(Color.WHITE);
            layout.setText(game.bodyFont, buttonText);
            game.bodyFont.draw(batch, buttonText,
                exitButton.x + (exitButton.width - layout.width) / 2,
                exitButton.y + (exitButton.height + layout.height) / 2);
        }


        batch.setColor(Color.WHITE);
    }

    private void handleGameInput() {
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


        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            returnToMenu();
        }
    }

    private void handleMouseInput(float mouseX, float mouseY) {
        if (Gdx.input.justTouched()) {
            if (exitButton.contains(mouseX, mouseY)) {
                returnToMenu();
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
    }
}
