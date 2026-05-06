package io.github.some_example_name;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.Random;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
	private Music backgroundMusic;
    private OrthographicCamera camera;
    private Viewport viewport; // Forces the 240x160 GBA ratio

    private Texture backgroundTexture;
    private Texture snowgroundTexture; // ADDED: The new snowground variable
    private Texture brokenIglooTexture;
    private Texture neapoIglooTexture;
    private Texture climbingIglooTexture;
    private float timer = 10f;
    private int gameState = 0; // 0 = Neapo Hiding, 1 = Bowler Guessing, 2 = Result, 3 = Game Over
    private int hiddenIgloo = -1; 
    private int guessIgloo = -1;  
    private Random random = new Random(); 
    
    private SpriteBatch batch;
    private GameCharacter player1; // Bowler
    private GameCharacter player2; // Neapo
    private BitmapFont font;
    private Texture iglooTexture;
    private Rectangle[] iglooHitboxes; 
    private Snowball snowball;
    private boolean snowballThrown = false;
    
    @Override
    public void create() {
        // 1. SETUP THE GBA CAMERA
        camera = new OrthographicCamera();
        viewport = new StretchViewport(240, 160, camera);
        
        batch = new SpriteBatch();
        
        // 2. NEW MINI COORDINATES FOR CHARACTERS
        player1 = new Snowman(90, 0); // Bowler bottom-left
        player2 = new Penguin(190, 40); // Neapo middle-right
        
        snowball = new Snowball();
        
        // Font needs to be tiny for the new screen size
        font = new BitmapFont(); 
        font.getData().setScale(0.5f); 
        
        iglooTexture = new Texture("iglooplaceholder.png");
        iglooHitboxes = new Rectangle[4];
        
        // 3. LOAD THE TEXTURES (Cleaned up duplicates)
        backgroundTexture = new Texture("Peppermint Palace Background.png");
        snowgroundTexture = new Texture("Snowground.png"); 
        
        climbingIglooTexture = new Texture("Igloo Sprite2.png");
        climbingIglooTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        
        brokenIglooTexture = new Texture("Broken Igloo.png");
        neapoIglooTexture = new Texture("Broken Igloo with Neapo.png");
        
        brokenIglooTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        neapoIglooTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Keep them crisp!
        backgroundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        snowgroundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // 4. NEW MINI COORDINATES FOR IGLOOS
     // 4. NEW MINI COORDINATES FOR IGLOOS (Taller, wider, and sitting higher!)
        for (int i = 0; i < 4; i++) {
            // x = 10 + (i * 57) keeps them perfectly spaced
            // y = 55 (pushes them higher up the screen)
            // width = 55, height = 50 (makes them chunkier)
            iglooHitboxes[i] = new Rectangle(2 + (i * 57), 75, 65, 50);
        }
        
     // 5. LOAD AND PLAY THE MUSIC!
        // Make sure this name EXACTLY matches the file in your assets folder
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("Snowball Scuffle - Kirby's Return to Dream Land Soundtrack.mp3")); 
        
        backgroundMusic.setLooping(true); // Loop forever
        backgroundMusic.setVolume(0.4f);  // Set volume (0.0 is silent, 1.0 is MAX)
        backgroundMusic.play();           // Start the music!
    }
    
    	

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

     // 1. CONSTANT MOUSE TRACKING (For Hovering)
        // ---------------------------------------------------------
        Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mousePos); 
        
        // Find out which igloo (if any) the mouse is touching right now
        int hoveredIgloo = -1;
        for (int i = 0; i < 4; i++) {
            if (iglooHitboxes[i].contains(mousePos.x, mousePos.y)) {
                hoveredIgloo = i;
            }
        }
        
     // NEAPO HOVER LOGIC! (Only when it is his turn to hide)
        if (gameState == 0 && hiddenIgloo == -1) {
            Penguin neapo = (Penguin)player2;
            
            if (hoveredIgloo != -1) {
                neapo.isVisible = true; // Show him!
                Rectangle box = iglooHitboxes[hoveredIgloo];
                
                // If it's one of the RIGHT two igloos (Igloo faces Left, Entrance is on the Left)
                if (hoveredIgloo >= 2) {
                    // Face Right (opposite of the igloo)
                    neapo.setFacingRight(true);
                    // Spawn him on the Left side (near the door)
                    neapo.setPosition(box.x - -10, box.y); 
                } else {
                    // If it's one of the LEFT two igloos (Igloo faces Right, Entrance is on the Right)
                    // Face Left (opposite of the igloo)
                    neapo.setFacingRight(false);
                    // Spawn him on the Right side (near the door)
                    neapo.setPosition(box.x + 20, box.y); 
                }
            } else {
                neapo.isVisible = false; // Hide if mouse is in the empty sky/snow
            }
        }

        // BOWLER HOVER LOGIC! (Only when he is guessing)
        if (gameState == 1 && guessIgloo == -1) {
            ((Snowman)player1).setFacingRight(mousePos.x > 120);
        }

        // 2. INPUT LOGIC (Clicks)
        // ---------------------------------------------------------
        if (Gdx.input.justTouched()) {
            for (int i = 0; i < 4; i++) {
                if (iglooHitboxes[i].contains(mousePos.x, mousePos.y)) {
                    if (gameState == 0 && hiddenIgloo == -1) {
                        hiddenIgloo = i + 1;
                        timer = 3f; 
                    } 
                    else if (gameState == 1 && guessIgloo == -1) {
                        guessIgloo = i + 1;
                        // Trigger the Snowman animation!
                        ((Snowman)player1).startThrow();
                        // Give the game 3 seconds total before resolving the turn
                        timer = 4.0f; 
                    }
                }
            }
        }

        // 3. MATH & UPDATE PHASE
        // ---------------------------------------------------------
        if (gameState != 3) { 
            timer -= delta;
            if (timer <= 0) {
                handleTimeout();
            }
        }
    
        
     // Update Bowler's animation timer
        ((Snowman)player1).update(delta);
        
     // TRIGGER EXACTLY ON FRAME 3 (But ONLY if an igloo is actually selected!)
        if (gameState == 1 && timer <= 1.0f && !snowballThrown && guessIgloo != -1) {
            // Find the center of the clicked igloo
            Rectangle target = iglooHitboxes[guessIgloo - 1];
            float targetX = target.x + (target.width / 2);
            float targetY = target.y + (target.height / 2);
            
            // Throw from Bowler's hand 
            snowball.throwSnowball(player1.x + 45, player1.y + 35, targetX, targetY);
            snowballThrown = true;
        }
        
        snowball.update(delta);
        
        // ... Keep your CLEAR TO BLACK and DRAWING PHASE exactly the same below this!

        // 3. DRAWING PHASE
        // ---------------------------------------------------------
        // CLEAR TO BLACK - NO MORE BLUE VOID!
        ScreenUtils.clear(0, 0, 0, 1); 
        
        // Tell the batch to use our GBA camera limits
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        
        batch.begin();
        
        // LAYER 0: The Palace Sky goes FIRST (Background)
        batch.draw(backgroundTexture, 0, 0, 240, 160);
        
        // LAYER 1: The Snowground goes SECOND (Midground)
        batch.draw(snowgroundTexture, 0, 0, 240, 160);
        
     // LAYER 2: The Igloos
        for (int i = 0; i < 4; i++) {
            boolean flipHorizontally = (i >= 2); 
            
            // HIGHLIGHT LOGIC
            if (hoveredIgloo == i && ((gameState == 0 && hiddenIgloo == -1) || (gameState == 1 && guessIgloo == -1))) {
                batch.setColor(0.7f, 1.0f, 0.7f, 1f); 
            } else {
                batch.setColor(1f, 1f, 1f, 1f); 
            }
            
         // --- NEW: CHOOSE WHICH TEXTURE TO DRAW ---
            Texture texToDraw = iglooTexture; // Default to normal igloo
            
            // 1. CLIMBING PHASE: Neapo just clicked an igloo and is hiding!
            if (gameState == 0 && hiddenIgloo != -1 && (hiddenIgloo - 1 == i)) {
                texToDraw = climbingIglooTexture;
            }
            // 2. REVEAL PHASE: The snowball hit!
            else if ((gameState == 2 || gameState == 3) && (guessIgloo - 1 == i)) {
                if (hiddenIgloo == guessIgloo) {
                    texToDraw = neapoIglooTexture; // We got her!
                } else {
                    texToDraw = brokenIglooTexture; // Empty destruction
                }
            }

            // Draw using whichever texture we selected!
            batch.draw(texToDraw, 
                iglooHitboxes[i].x, iglooHitboxes[i].y,     
                iglooHitboxes[i].width, iglooHitboxes[i].height, 
                0, 0,                                       
                texToDraw.getWidth(), texToDraw.getHeight(), 
                flipHorizontally, false                     
            );
            
            batch.setColor(1f, 1f, 1f, 1f);
        }

        // LAYER 3: The Characters
        player1.draw(batch); 

        if (gameState == 0 && hiddenIgloo == -1) {
            player2.draw(batch);
        }
        
     // LAYER 3.5: The Snowball
        snowball.draw(batch);

        // LAYER 4: THE HUD (UI)
        // ... (Keep your font drawing logic here)

        // LAYER 4: THE HUD (UI)
        if (gameState == 0) {
            font.draw(batch, "NEAPO HIDING (CLICK AN IGLOO)", 30, 140);
            font.draw(batch, "TIME: " + (int)timer, 100, 120);
        } else if (gameState == 1) {
            font.draw(batch, "BOWLER GUESSING...", 70, 140);
            font.draw(batch, "TIME: " + (int)timer, 100, 120);
        } else if (gameState == 2) {
            font.draw(batch, "THE MOMENT OF TRUTH!", 50, 140);
        } else if (gameState == 3) {
            String winner = (player1.getScore() >= 3) ? "BOWLER WINS!" : "NEAPO WINS!";
            font.draw(batch, winner, 80, 100);
            font.draw(batch, "PRESS R TO RESTART", 60, 80);
        }

        font.draw(batch, "Bowler: " + player1.getScore(), 5, 155);
        font.draw(batch, "Neapo: " + player2.getScore(), 180, 155);
            
        batch.end();

        // 4. RESTART LOGIC
        // ---------------------------------------------------------
        if (gameState == 3 && Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.R)) {
            restartGame();
        }
    }

    // REQUIRED FOR VIEWPORTS: Tells the camera to adjust if the window is resized
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    private void handleTimeout() {
        if (gameState == 0) { 
            if (hiddenIgloo == -1) hiddenIgloo = random.nextInt(4) + 1;
            gameState = 1;
            timer = 8f; 
        } 
        else if (gameState == 1) { 
            if (guessIgloo == -1) {
                // THE AFK FIX: Player didn't choose in time!
                guessIgloo = random.nextInt(4) + 1;
                
                // Force Bowler to actually look at the igloo he randomly picked
                // (Igloos 3 and 4 are on the right side of the screen)
                ((Snowman)player1).setFacingRight(guessIgloo > 2);
                
                // Trigger the animation and reset the clock so the throw happens normally!
                ((Snowman)player1).startThrow();
                timer = 4f; 
            } else {
                // THE IMPACT PHASE: The snowball finished flying! Transition to Reveal.
                gameState = 2;
                timer = 2.5f; 
            }
        }
        else if (gameState == 2) {
            checkWinner();
        }
    }

    private void checkWinner() {
        if (hiddenIgloo == guessIgloo) {
            player1.addPoint();
        } else {
            player2.addPoint();
        }
        
        if (player1.getScore() >= 3 || player2.getScore() >= 3) {
            gameState = 3; 
            ((Snowman)player1).isThrowing = false; // Reset animation for Game Over screen!
        } else {
            gameState = 0;
            timer = 8f; 
            hiddenIgloo = -1;
            guessIgloo = -1;
            
            // Clean up the projectiles and animations for the next round!
            snowball.active = false;
            snowballThrown = false;
            ((Snowman)player1).isThrowing = false; 
        }
    }
    
    private void restartGame() {
        gameState = 0;
        timer = 8f;
        hiddenIgloo = -1;
        guessIgloo = -1;
        
        // Clean up everything on a manual restart
        snowball.active = false;
        snowballThrown = false;
        ((Snowman)player1).isThrowing = false;
        
        // NEW: Actually wipe the scoreboard clean!
        player1.resetScore();
        player2.resetScore();
    }

    @Override
    public void dispose() {
        batch.dispose();
        player1.dispose();
        player2.dispose();
        backgroundTexture.dispose();
        snowgroundTexture.dispose(); // ADDED: Clean up the new texture
        iglooTexture.dispose();
        brokenIglooTexture.dispose();
        neapoIglooTexture.dispose();
        climbingIglooTexture.dispose();
        backgroundMusic.dispose();
        font.dispose();
        snowball.dispose();
    }
}