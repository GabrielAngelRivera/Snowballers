package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.Random;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
	private float timer = 10f;
	private int gameState = 0; // 0 = Neapo Hiding, 1 = Bowler Guessing, 2 = Result
	private int hiddenIgloo = -1; // The slot Neapo picks
	private int guessIgloo = -1;  // The slot Bowler picks
	private Random random = new Random(); // For the "Auto-Pick" logic
    private SpriteBatch batch;
    private GameCharacter player1; //Bowler
    private GameCharacter player2; //Neapo
    private BitmapFont font;
    private Texture iglooTexture;
    private Rectangle[] iglooHitboxes; // This tells Java WHERE the click happens

 // Inside Main.java
    @Override
    public void create() {
        batch = new SpriteBatch();
        
        // player1 is Bowler (the Snowman)
        // We start him off-screen to the left and slightly down to get that close-up look.
        player1 = new Snowman(0, 0); 
        
        // player2 is Neapo (the Penguin)
        // We push her way to the right and slightly up, matching image_11.png
        player2 = new Penguin(600, 200); 
        
        font = new BitmapFont(); // Loads the standard LibGDX font
        font.getData().setScale(4f); // Makes the text big enough to read in 1080p
        
        iglooTexture = new Texture("iglooplaceholder.png");
        iglooHitboxes = new Rectangle[4];

        for (int i = 0; i < 4; i++) {
            // This spreads them out: x starts at 400, then 750, 1100, 1450
            iglooHitboxes[i] = new Rectangle(400 + (i * 350), 300, 300, 300);
        }
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // 1. INPUT LOGIC: Checking for Igloo Clicks
        // ---------------------------------------------------------
        if (Gdx.input.justTouched()) {
            float mouseX = Gdx.input.getX();
            // Flip Y because LibGDX coordinates start at the bottom
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

            for (int i = 0; i < 4; i++) {
                if (iglooHitboxes[i].contains(mouseX, mouseY)) {
                    // Neapo's turn to hide
                    if (gameState == 0 && hiddenIgloo == -1) {
                        hiddenIgloo = i + 1;
                        timer = 3f; // She "hides" for 3 seconds
                    } 
                    // Bowler's turn to guess
                    else if (gameState == 1 && guessIgloo == -1) {
                        guessIgloo = i + 1;
                        timer = 2f; // Snowball "travels" for 2 seconds
                    }
                }
            }
        }

        // 2. MATH PHASE: Timer & State Transitions
        // ---------------------------------------------------------
        if (gameState != 3) { // Only run if the game isn't over
            timer -= delta;
            if (timer <= 0) {
                handleTimeout();
            }
        }

        // 3. DRAWING PHASE: Background to Foreground
        // ---------------------------------------------------------
        ScreenUtils.clear(0.8f, 0.9f, 1f, 1f); 
        batch.begin();
        
        // Layer 1: The Igloos (Background)
        for (int i = 0; i < 4; i++) {
            batch.draw(iglooTexture, iglooHitboxes[i].x, iglooHitboxes[i].y, 300, 300);
        }

        // Layer 2: The Characters
        player1.draw(batch); // Bowler stays on screen

        // Hide Neapo if she has picked an igloo or it's Bowler's turn
        if (gameState == 0 && hiddenIgloo == -1) {
            player2.draw(batch);
        }

        // Layer 3: THE HUD (UI)
        if (gameState == 0) {
            font.draw(batch, "NEAPO HIDING (CLICK AN IGLOO)...", 500, 1050);
            font.draw(batch, "TIME: " + (int)timer, 880, 950);
        } else if (gameState == 1) {
            font.draw(batch, "BOWLER GUESSING...", 750, 1050);
            font.draw(batch, "TIME: " + (int)timer, 880, 950);
        } else if (gameState == 2) {
            font.draw(batch, "THE MOMENT OF TRUTH!", 700, 1050);
        } else if (gameState == 3) {
            String winner = (player1.getScore() >= 3) ? "BOWLER WINS THE MATCH!" : "NEAPO WINS THE MATCH!";
            font.draw(batch, winner, 650, 600);
            font.draw(batch, "PRESS R TO RESTART", 750, 500);
        }

        // Always draw persistent scores
        font.draw(batch, "Bowler: " + player1.getScore(), 100, 1050);
        font.draw(batch, "Neapo: " + player2.getScore(), 1550, 1050);
            
        batch.end();

        // 4. RESTART LOGIC
        // ---------------------------------------------------------
        if (gameState == 3 && Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.R)) {
            restartGame();
        }
    }

    // PASTE THIS HERE:
    private void handleTimeout() {
        if (gameState == 0) { 
            if (hiddenIgloo == -1) hiddenIgloo = random.nextInt(4) + 1;
            gameState = 1;
            timer = 10f;
        } 
        else if (gameState == 1) { 
            if (guessIgloo == -1) guessIgloo = random.nextInt(4) + 1;
            gameState = 2;
            checkWinner();
        }
    }

    private void checkWinner() {
        if (hiddenIgloo == guessIgloo) {
            player1.addPoint();
        } else {
            player2.addPoint();
        }
        
        // Check for the match point
        if (player1.getScore() >= 3 || player2.getScore() >= 3) {
            gameState = 3; // GAME OVER state
        } else {
            // Keep playing
            gameState = 0;
            timer = 10f;
            hiddenIgloo = -1;
            guessIgloo = -1;
        }
    }
    
    private void restartGame() {
        gameState = 0;
        timer = 10f;
        hiddenIgloo = -1;
        guessIgloo = -1;
        // We'd need to add a resetScore() in GameCharacter to fully reset
    }

    @Override
    public void dispose() {
        batch.dispose();
        player1.dispose();
        player2.dispose();
    }
}


