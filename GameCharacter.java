package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class GameCharacter {
    protected float x, y;
    protected Texture sprite;
    private int score = 0;

    // CONSTRUCTOR 1 (sprite-sheet): only sets coordinates; the child class handles its own image loading (Bowler)
    public GameCharacter(float x, float y) {
        this.x = x;
        this.y = y;
    }

    
    // CONSTRUCTOR 2 (single sprite/direct texture): sets coordinates AND loads the old school texture (Neapo)
    public GameCharacter(float x, float y, String texturePath) {
        this.x = x;
        this.y = y;
        this.sprite = new Texture(texturePath);
    }

    // adds 1 to the score
    public void addPoint() {
        score++; //increases score safely with no direct access
    }

    // returns the current score to whoever asks, but can NOT modify it (like Main.java)
    public int getScore() {
        return score;
    }
    
 // instantly resets the character's score to 0
    public void resetScore() {
        this.score = 0;
    }

    // -------------------------------------

    public abstract void draw(SpriteBatch batch);

    public void dispose() {
        if (sprite != null) {
            sprite.dispose();
        }
    }

		
	}
