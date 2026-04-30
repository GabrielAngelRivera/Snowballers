package io.github.some_example_name;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// 'extends' is the keyword for INHERITANCE
public class Snowman extends GameCharacter {

    public Snowman(float x, float y) {
        // 'super' sends the coordinates and image name to the parent GameCharacter
        super(x, y, "BowlerTest2.png"); 
    }

 // Inside Snowman.java
    @Override
    public void draw(SpriteBatch batch) {
        // We are nearly doubling his size (keeping the correct 16:9 ratio)
        // to make him fill the left side like a boss.
        batch.draw(sprite, x, y, 1650, 928);
    }
}