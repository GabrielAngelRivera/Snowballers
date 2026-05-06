package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Snowman extends GameCharacter {
    private Texture sheet;
    private TextureRegion[] frames;
    
    private boolean facingRight = true;
    public boolean isThrowing = false;
    private float throwTimer = 0f;
    private int currentFrame = 0;

    public Snowman(float x, float y) {
    	super(x, y); // Just add an empty string! // Assuming your GameCharacter has an x,y constructor
        
        // 1. Load the Sprite Sheet
        sheet = new Texture("BowlerWSnowball Sprite Sheet.png");
        sheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        
        // 2. Chop the sheet into 3 equal frames
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / 3, sheet.getHeight());
        frames = new TextureRegion[3];
        frames[0] = tmp[0][0]; // Idle
        frames[1] = tmp[0][1]; // Windup
        frames[2] = tmp[0][2]; // Throw
    }

    // Call this from Main when an igloo is clicked
    public void startThrow() {
        isThrowing = true;
        throwTimer = 0f;
    }

    public void setFacingRight(boolean facingRight) {
        // Only let him turn if he isn't in the middle of a throw!
        if (!isThrowing) {
            this.facingRight = facingRight;
        }
    }

    public void update(float delta) {
        if (isThrowing) {
            throwTimer += delta;
            
            // Wait 2 seconds, then load the Windup sprite
            if (throwTimer >= 2.0f && throwTimer < 3.0f) {
                currentFrame = 1; 
            } 
            // 1 second later (3 seconds total), load the Throw sprite
            else if (throwTimer >= 3.0f) {
                currentFrame = 2;
            } else {
                currentFrame = 0; // The initial 2-second wait (Idle)
            }
        } else {
            currentFrame = 0; // Default to idle
            throwTimer = 0f;
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion frameToDraw = frames[currentFrame];

        // This checks if the image needs to be flipped horizontally 
        // to match where the mouse is!
        if (frameToDraw.isFlipX() == facingRight) {
            frameToDraw.flip(true, false);
        }

        // Draw him! You might need to adjust the 60, 60 size to fit perfectly
        batch.draw(frameToDraw, x, y, 70, 60); 
    }

    @Override
    public void dispose() {
        sheet.dispose();
    }
}