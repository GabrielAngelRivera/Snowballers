package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class GameCharacter {
    protected float x, y;
    protected Texture sprite;
    
    // PILLAR: ENCAPSULATION
    // This is kept private so only this class can touch the raw number.
    private int score = 0;

 /// CONSTRUCTOR 1: The "Snowman" Way (Advanced Sprite Sheets)
    // Only sets coordinates. The child class handles its own image loading.
    public GameCharacter(float x, float y) {
        this.x = x;
        this.y = y;
    }

    // CONSTRUCTOR 2: The "Neapo" Way (Simple Single Images)
    // Sets coordinates AND loads the old-school texture.
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
    
 // Instantly resets the character's score to 0
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
