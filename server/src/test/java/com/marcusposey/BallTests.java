package com.marcusposey;

import org.junit.Assert;
import org.junit.Test;

/** Tests for the Ball class */
public class BallTests {
    /** The ball should not signal collision if it isn't touching a player. */
    @Test
    public void testCollision_noCollide() {
        Ball ball = new Ball();
        final int ballXPos = 3;
        ball.setX(ballXPos);
        ball.setY(1);

        Player player = new Player(null);
        player.setX(ball.getX() + ball.getWidth() + 100);
        player.setY(ball.getY() + ball.getHeight() + 100);

        ball.move(new Player[]{player, player});
        ball.move(new Player[]{player, player});
        // It would change x direction if an intersection occurred.
        Assert.assertTrue(ball.getX() > ballXPos);
    }

    /** The ball should signal collision if it is touching a player. */
    @Test
    public void testCollision_collide() {
        Ball ball = new Ball();
        final int ballXPos = 3;        
        ball.setX(ballXPos);
        ball.setY(1);

        Player player = new Player(null);
        player.setX(ball.getX());
        player.setY(ball.getY());

        ball.move(new Player[]{player, player});
        ball.move(new Player[]{player, player});
        
        // It would change x direction if an intersection occurred.
        Assert.assertTrue(ball.getX() <= ballXPos);
    }

    /** Ensure the ball resets if it goes out of the map bounds */
    @Test
    public void testReset() {
        Ball ball = new Ball();
        
        // x coordinates outside of the map bounds
        final int[] kBoundsConfigs = new int[]{
            0 - ball.getWidth() - 100,
            GameState.kCanvasWidth + ball.getWidth() + 100
        };
        for (final int config : kBoundsConfigs) {
            // Set it out of the map.
            ball.setX(config);

            final boolean isReset = ball
                .move(new Player[]{new Player(null), new Player(null)});

            // Hopefully it actually detects out of bounds events.
            Assert.assertTrue(isReset);

            // Make sure it was put back in the map.
            Assert.assertTrue(ball.getX() < GameState.kCanvasWidth);
            Assert.assertTrue(ball.getX() > 0);
        }
    }
}