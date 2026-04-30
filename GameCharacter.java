package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class GameCharacter {
    protected float x, y;
    protected Texture sprite;
    
    // PILLAR: ENCAPSULATION
    // This is kept private so only this class can touch the raw number.
    private int score = 0;

    public GameCharacter(float x, float y, String texturePath) {
        this.x = x;
        this.y = y;
        this.sprite = new Texture(texturePath);
    }

    // --- NEW TOOLS FOR THE GAME LOGIC ---

    // Adds 1 to the score
    public void addPoint() {
        score++;
    }

    // Returns the current score to whoever asks (like Main.java)
    public int getScore() {
        return score;
    }

    // -------------------------------------

    public abstract void draw(SpriteBatch batch);

    public void dispose() {
        if (sprite != null) {
            sprite.dispose();
        }
    }
}