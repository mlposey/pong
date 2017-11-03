package com.marcusposey;

import java.util.concurrent.ThreadLocalRandom;

/** Handles a ball's location and physics */
public class Ball extends Entity {
    private final int kSpeed = 4;
    private int xVelocity = 3;
    private int yVelocity = 3;

    // The last player to hit the ball
    // 0 = left player; 1 = right player
    private int lastToHit = 0;

    /** Creates a new ball with a default size and location */
    public Ball() {
        super((GameState.kCanvasWidth - 20) / 2,
                (GameState.kCanvasHeight - 20) / 2, 20, 20);
    }

    /**
     * Moves the ball according to its physics
     * @param players either player that the ball may hit and bounce away from
     * @return true if the move resulted in the ball going out of the y bounds
     */
    public boolean move(final Entity[] players) {
        setX(getX() + xVelocity);
        setY(getY() + yVelocity);
        if (getY() < 0 || getY() + getWidth() > GameState.kCanvasHeight) {
            yVelocity *= -1;
        }
        lastToHit = xVelocity < 0 ? 1 : 0;

        final int handle = xVelocity < 0 ? 0 : 1;
        if (intersects(players[handle])) {
            // Bounce the ball in the other direction.
            xVelocity = -xVelocity;
        } else if (getX() + getWidth() < 0 || getX() > GameState.kCanvasWidth) {
            // They missed the ball.
            spawnBall();
            return true;
        }
        return false;
    }

    /**
     * Returns a handle to the last player to hit the ball
     */
    public int getLastToHit() {
        return lastToHit;
    }

    /** Spawns the ball in the horizontal center of the map */
    private void spawnBall() {
        setX((GameState.kCanvasWidth - getWidth()) / 2);
        setY(ThreadLocalRandom.current().nextInt(0, GameState.kCanvasHeight - getWidth()));

        // Go somewhere up or down.
        yVelocity = (ThreadLocalRandom.current().nextBoolean() ? 1 : -1) * 3;

        // Send it towards the player who lost the last match.
        final int newDirection = getX() > GameState.kCanvasWidth ? 1 : -1;
        xVelocity = newDirection * 3;
    }

    /** Returns true if the ball is intersecting with entity */
    private boolean intersects(final Entity entity) {
        return  entity.getX() < getX() + getWidth()        &&
                entity.getY() < getY() + getHeight()       &&
                getX() < entity.getX() + entity.getWidth() &&
                getY() < entity.getY() + entity.getHeight();
    }
}
