package io.github.some_example_name;

import com.badlogic.gdx.audio.Music; // used for long audio files, streams file from disk to save memory.
import com.badlogic.gdx.ApplicationAdapter; // upon extending this, Main gets access to methods such as create(), render(), and dispose()
import com.badlogic.gdx.Gdx; // provides access to tools such as Gdx.input (for mouse) and Gdx.files (loading images)
import com.badlogic.gdx.graphics.OrthographicCamera; // this is what the player sees, camera in a 2D game, no perspective or 3D depth
import com.badlogic.gdx.graphics.Texture; // represents single images loaded into the VRAM, like a background or igloo
import com.badlogic.gdx.graphics.g2d.BitmapFont; // handles drawing text (texture atlas)
import com.badlogic.gdx.graphics.g2d.SpriteBatch; // collects multiple drawing commands, sends them to the GPU simultaneously to make game run smoothly
import com.badlogic.gdx.math.Rectangle; // used to define hit-boxes for the igloos
import com.badlogic.gdx.math.Vector2; // used to track x and y coordinates of mouse position
import com.badlogic.gdx.utils.ScreenUtils; // cleans the screen, wipes out previous frames
import com.badlogic.gdx.utils.viewport.StretchViewport; // gives the game a retro vibe; ensures the game is 240x160 regardless of resizing
import com.badlogic.gdx.utils.viewport.Viewport; // parent class for all viewport types, manages the camera's relationship to the screen
import java.util.Random; // standard java tool for randomization; primarily used for afk logic

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
	// All of these are made private to satisfy encapsulation!
	private Music backgroundMusic; // the games background music
    private OrthographicCamera camera; // what the player sees
    private Viewport viewport; // forces the 240x160 GBA ratio

    private Texture backgroundTexture; // images utilized in the game
    private Texture snowgroundTexture; 
    private Texture brokenIglooTexture;
    private Texture neapoIglooTexture;
    private Texture climbingIglooTexture;
    private float timer = 10f; // countdown timer; starts at 10 seconds 
    private int gameState = 0; // 0 = Neapo Hiding, 1 = Bowler Guessing, 2 = Result, 3 = Game Over
    private int hiddenIgloo = -1; // where Neapo is hiding, -1 means nothing has been chosen yet
    private int guessIgloo = -1;  // what Bowler picks
    private Random random = new Random(); // randomly picks spot
    
    private SpriteBatch batch; // draws everything on the screen
    private GameCharacter player1; // Bowler
    private GameCharacter player2; // Neapo
    private BitmapFont font; // draws out text
    private Texture iglooTexture; 
    private Rectangle[] iglooHitboxes; 
    private Snowball snowball; 
    private boolean snowballThrown = false; // did we throw snowball yet?
    
    @Override
    public void create() {
        // 1. SETTING UP THE GBA CAMERA
        camera = new OrthographicCamera();
        viewport = new StretchViewport(240, 160, camera);
        
        batch = new SpriteBatch();
        
        // 2. COORDINATES FOR CHARACTERS TO FIT 240X160 RESOLUTION
        player1 = new Snowman(90, 0); // Bowler's position
        player2 = new Penguin(190, 40); // Neapo's position
        
        snowball = new Snowball();
        
        // font needs to be tiny for the new screen size
        font = new BitmapFont(); 
        font.getData().setScale(0.5f); 
        
        iglooTexture = new Texture("iglooplaceholder.png");
        iglooHitboxes = new Rectangle[4];
        
        // 3. LOAD THE TEXTURES
        backgroundTexture = new Texture("Peppermint Palace Background.png");
        snowgroundTexture = new Texture("Snowground.png"); 
        
        climbingIglooTexture = new Texture("Igloo Sprite2.png");
        climbingIglooTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest); // we use "Nearest" to stop the engine from blending pixels, keeping them sharp and blocky
        
        brokenIglooTexture = new Texture("Broken Igloo.png");
        neapoIglooTexture = new Texture("Broken Igloo with Neapo.png");
        
        brokenIglooTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        neapoIglooTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // keep the pixels crisp!
        backgroundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        snowgroundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // 4. COORDINATES FOR IGLOOS
        for (int i = 0; i < 4; i++) {
            // x = 2 + (i * 57) keeps them perfectly spaced apart from each other
            // y = 75 (pushes them higher up the screen)
            // width = 65, height = 50 (makes them wider)
            iglooHitboxes[i] = new Rectangle(2 + (i * 57), 75, 65, 50);
        }
        
        // 5. LOAD AND PLAYING THE MUSIC
  
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("Snowball Scuffle - Kirby's Return to Dream Land Soundtrack.mp3")); 
        
        backgroundMusic.setLooping(true); // loops the song forever
        backgroundMusic.setVolume(0.4f);  // sets the volume 
        backgroundMusic.play();           // starts the music
    }
    
    	

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime(); 

     // 1. CONSTANT MOUSE TRACKING (For Hovering)
        Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mousePos); //unproject translates "screen cords" (where mouse is on monitor) into "world coords" (where mouse is in game)
        
        // Find out which igloo (if any) the mouse is touching right now
        int hoveredIgloo = -1;
        for (int i = 0; i < 4; i++) {
            if (iglooHitboxes[i].contains(mousePos.x, mousePos.y)) {
                hoveredIgloo = i;
            }
        }
        
     // NEAPO HOVER LOGIC (Only when it is her turn to hide)
        if (gameState == 0 && hiddenIgloo == -1) { // if it is Neapo's turn to hide and if an igloo hasn't been chosen yet
            Penguin neapo = (Penguin)player2; //Neapo's game
            
            if (hoveredIgloo != -1) { // if we are hovering over an igloo
                neapo.isVisible = true; // show Neapo
                Rectangle box = iglooHitboxes[hoveredIgloo]; 
                
                // if it's one of the RIGHT two igloos (igloo faces left, entrance is on the left)
                if (hoveredIgloo >= 2) {
                    // face Right (opposite of the igloo)
                    neapo.setFacingRight(true);
                    // spawn Neapo on the Left side (near the door)
                    neapo.setPosition(box.x - -10, box.y); 
                } else {
                    // If it's one of the LEFT two igloos (igloo faces Right, entrance is on the right)
                    // face Left (opposite of the igloo)
                    neapo.setFacingRight(false);
                    // spawn Neapo on the Right side (near the door)
                    neapo.setPosition(box.x + 20, box.y); 
                }
            } else {
                neapo.isVisible = false; // Hide if mouse is in the empty sky/snow
            }
        }

        // BOWLER HOVER LOGIC (Only when he is guessing)
        if (gameState == 1 && guessIgloo == -1) {
            ((Snowman)player1).setFacingRight(mousePos.x > 120); //faces right or left depending on mouse position
        }

        // 2. INPUT LOGIC (Clicks)
        if (Gdx.input.justTouched()) { //triggers once per click
            for (int i = 0; i < 4; i++) {
                if (iglooHitboxes[i].contains(mousePos.x, mousePos.y)) {
                    if (gameState == 0 && hiddenIgloo == -1) {
                        hiddenIgloo = i + 1; // stores which igloo Neapo is hiding in after Neapo chooses
                        timer = 3f; // starts a countdown
                    } 
                    else if (gameState == 1 && guessIgloo == -1) {
                        guessIgloo = i + 1;
                        // triggers the Snowman animation
                        ((Snowman)player1).startThrow();
                        // gives the game 4 seconds total before resolving the turn
                        timer = 4.0f; 
                    }
                }
            }
        }

        // 3. MATH & UPDATE PHASE
        if (gameState != 3) { // if the game isn't over
            timer -= delta; // timer counts down every frame
            if (timer <= 0) {
                handleTimeout(); // move on to next game phase
            }
        }
    
        
     // update Bowler's animation timer
        ((Snowman)player1).update(delta);
        
     // TRIGGERS EXACTLY ON FRAME 3 (But ONLY if an igloo is actually selected)
        if (gameState == 1 && timer <= 1.0f && !snowballThrown && guessIgloo != -1) {
            // find the center of the clicked igloo
            Rectangle target = iglooHitboxes[guessIgloo - 1];
            float targetX = target.x + (target.width / 2);
            float targetY = target.y + (target.height / 2);
            
            // throw from Bowler's hand 
            snowball.throwSnowball(player1.x + 45, player1.y + 35, targetX, targetY);
            snowballThrown = true;
        }
        
        snowball.update(delta);

        // 4. DRAWING PHASE
        ScreenUtils.clear(0, 0, 0, 1); 
        
        // Tell the batch to use our GBA camera limits
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        
        batch.begin(); //begin drawing
        
        // LAYER 0: The Peppermint Palace Background (GOES FIRST SINCE IT'S A BACKGROUND)
        batch.draw(backgroundTexture, 0, 0, 240, 160);
        
        // LAYER 1: The Snowground (GOES SECOND SINCE IT'S MIDGROUND/WHERE OUR PLAYERS WILL BE)
        batch.draw(snowgroundTexture, 0, 0, 240, 160);
        
        // LAYER 2: The Igloos
        for (int i = 0; i < 4; i++) {
            boolean flipHorizontally = (i >= 2); 
            
            // HIGHLIGHT LOGIC
            if (hoveredIgloo == i && ((gameState == 0 && hiddenIgloo == -1) || (gameState == 1 && guessIgloo == -1))) { 
                batch.setColor(0.7f, 1.0f, 0.7f, 1f); //highlight color
            } else {
                batch.setColor(1f, 1f, 1f, 1f); //no highlight
            }
            
            Texture texToDraw = iglooTexture; // Default to normal igloo
            
            // 1. CLIMBING IN PHASE: Neapo just clicked an igloo and is hiding
            if (gameState == 0 && hiddenIgloo != -1 && (hiddenIgloo - 1 == i)) {
                texToDraw = climbingIglooTexture; //shows us Neapo crawling in the igloo
            }
            
            // 2. REVEAL PHASE: The snowball hit!
            else if ((gameState == 2 || gameState == 3) && (guessIgloo - 1 == i)) {
                if (hiddenIgloo == guessIgloo) {
                    texToDraw = neapoIglooTexture; // Neapo is hit
                } else {
                    texToDraw = brokenIglooTexture; // Wrong igloo
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
            player2.draw(batch); //Neapo is only available during the hover phase
        }
        
        // LAYER 3.5: The Snowball
        snowball.draw(batch);


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
        if (gameState == 3 && Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.R)) {
            restartGame();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    private void handleTimeout() {
        if (gameState == 0) { 
            if (hiddenIgloo == -1) hiddenIgloo = random.nextInt(4) + 1; //afk protection; prevents game from stalling
            gameState = 1; // game state changes
            timer = 8f; // timer restarts
        } 
        else if (gameState == 1) { 
            if (guessIgloo == -1) {
                // THE AFK FIX: Player didn't choose in time
                guessIgloo = random.nextInt(4) + 1;
                
                // force Bowler to actually look at the igloo he randomly picked
                // igloos 3 and 4 are on the right side of the screen
                ((Snowman)player1).setFacingRight(guessIgloo > 2);
                
                // trigger the animation and resets the clock so the throw happens normally
                ((Snowman)player1).startThrow();
                timer = 4f; //forcing throw sequence to play
            } else {
                // THE IMPACT PHASE: the snowball finishes flying; transition to the reveal
                gameState = 2; //change current game state to 2 (reveal phase)
                timer = 2.5f; //pauses to reveal
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
            ((Snowman)player1).isThrowing = false; // reset animation for game over screen
        } else {
            gameState = 0; //starts next round, resetting everything
            timer = 8f; 
            hiddenIgloo = -1;
            guessIgloo = -1;
            
            // clean up the projectiles and animations for the next round!
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
        
        // clean up everything on a manual restart
        snowball.active = false;
        snowballThrown = false;
        ((Snowman)player1).isThrowing = false;
        
        // wipes the scoreboard clean!
        player1.resetScore();
        player2.resetScore();
    }

    @Override
    public void dispose() { //preventing memory leaks (freeing resources)
        batch.dispose();
        player1.dispose();
        player2.dispose();
        backgroundTexture.dispose();
        snowgroundTexture.dispose();
        iglooTexture.dispose();
        brokenIglooTexture.dispose();
        neapoIglooTexture.dispose();
        climbingIglooTexture.dispose();
        backgroundMusic.dispose();
        font.dispose();
        snowball.dispose();
    }
}