package io.github.some_example_name;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Penguin extends GameCharacter {
    private TextureRegion region;
    private boolean facingRight = true;
    public boolean isVisible = false; // start hidden until the player hovers

    public Penguin(float x, float y) {
        // calls the "Neapo constructor" in GameCharacter to load the NEW image
        super(x, y, "Neapo Sprite2.png"); 
        // wrap the loaded image in a Region so we can flip it
        region = new TextureRegion(this.sprite); 
    }

    // teleports Neapo to the hovered igloo
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    @Override
    public void draw(SpriteBatch batch) {
        // only draw Neapo if the mouse is touching an igloo
        if (!isVisible) return;

        // flip texture region if it doesn't match the direction he should face
        if (region.isFlipX() == facingRight) {
            region.flip(true, false);
        }

        // draw Neapo 
        batch.draw(region, x, y, 35, 30); 
    }
}