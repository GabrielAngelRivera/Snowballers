package io.github.some_example_name;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Penguin extends GameCharacter {

    public Penguin(float x, float y) {
        // Use the exact name of your Neapo image here
        super(x, y, "NeapoTest1.png"); 
    }

 // Inside Penguin.java
    @Override
    public void draw(SpriteBatch batch) {
        // Making Neapo beefier so she doesn't look like a speck next to massive Bowler
        batch.draw(sprite, x, y, 1200, 676);
    }
}