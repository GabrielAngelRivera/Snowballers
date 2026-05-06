package io.github.some_example_name;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Penguin extends GameCharacter {
    private TextureRegion region;
    private boolean facingRight = true;
    public boolean isVisible = false; // Start hidden until the player hovers!

    public Penguin(float x, float y) {
        // Calls the "Neapo constructor" in GameCharacter to load the NEW image
        super(x, y, "Neapo Sprite2.png"); 
        // Wrap the loaded image in a Region so we can flip it
        region = new TextureRegion(this.sprite); 
    }

    // Teleports Neapo to the hovered igloo
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Only draw him if the mouse is touching an igloo!
        if (!isVisible) return;

        // Flip texture region if it doesn't match the direction he should face
        if (region.isFlipX() == facingRight) {
            region.flip(true, false);
        }

        // Draw Neapo (sized to 30x45 to fit his new taller hat proportions nicely)
        batch.draw(region, x, y, 35, 30); 
    }
}