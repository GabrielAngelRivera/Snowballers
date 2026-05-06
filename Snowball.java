package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Snowball {
    private Texture texture;
    public boolean active = false;
    
    // Position tracking
    public float x, y;
    private float startX, startY, targetX, targetY;
    
    // Physics & Timing Variables
    private float flightTimer = 0f;
    private float flightDuration = 0.6f; // Takes 0.6 seconds to hit the igloo
    private float currentScale = 1.0f;
    private float arcHeight = 45f; // How high the snowball arcs in pixels

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
            
            // "t" is a percentage from 0.0 (start) to 1.0 (end)
            float t = flightTimer / flightDuration;

            if (t >= 1.0f) {
                t = 1.0f; // Cap it so it doesn't overshoot
                active = false; // It hit the target!
            }

            // 1. Calculate the straight line (Lerp)
            float currentX = startX + (targetX - startX) * t;
            float baseY = startY + (targetY - startY) * t;

            // 2. Add the Parabolic Arc (Using a Sine wave)
            // Math.PI makes a perfect half-circle curve from 0 to 1
            float arcOffset = (float)Math.sin(t * Math.PI) * arcHeight;

            // Apply positions
            this.x = currentX;
            this.y = baseY + arcOffset;

            // 3. Shrink the snowball as it flies into the background
            // Starts at 1.0 size, shrinks down to 0.3 size
            this.currentScale = 1.0f - (0.7f * t); 
        }
    }

    public void draw(SpriteBatch batch) {
        if (active) {
            // Base size is 16x16, multiplied by our shrinking scale
            float size = 16f * currentScale;
            
            // Draw it centered on the x,y coordinates
            batch.draw(texture, x - (size / 2), y - (size / 2), size, size);
        }
    }

    public void dispose() {
        texture.dispose();
    }
}