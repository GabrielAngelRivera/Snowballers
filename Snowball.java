package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Snowball {
    private Texture texture;
    public boolean active = false;
    
    // position tracking
    public float x, y;
    private float startX, startY, targetX, targetY;
    
    // physics & Timing Variables
    private float flightTimer = 0f;
    private float flightDuration = 0.6f; // takes 0.6 seconds to hit the igloo
    private float currentScale = 1.0f;
    private float arcHeight = 45f; // how high the snowball arcs in pixels

    public Snowball() {
        texture = new Texture("Snowball Sprite.png");
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    public void throwSnowball(float startX, float startY, float targetX, float targetY) {
        this.startX = startX;
        this.startY = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.x = startX;
        this.y = startY;
        this.flightTimer = 0f;
        this.active = true;
    }

    public void update(float delta) {
        if (active) {
            flightTimer += delta;
            
            float t = flightTimer / flightDuration;

            if (t >= 1.0f) {
                t = 1.0f; 
                active = false; 
            }

      
            float currentX = startX + (targetX - startX) * t;
            float baseY = startY + (targetY - startY) * t;

            float arcOffset = (float)Math.sin(t * Math.PI) * arcHeight;

            this.x = currentX;
            this.y = baseY + arcOffset;

            this.currentScale = 1.0f - (0.7f * t); 
        }
    }

    public void draw(SpriteBatch batch) {
        if (active) {

            float size = 16f * currentScale;
            
            batch.draw(texture, x - (size / 2), y - (size / 2), size, size);
        }
    }

    public void dispose() {
        texture.dispose();
    }
}