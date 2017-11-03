package com.marcusposey;

import org.junit.Test;

import com.marcusposey.Player.Direction;

import org.junit.Assert;

/** Tests for the Player class */
public class PlayerTests {
    /** The player should always stay within bounds of the map */
    @Test
    public void testMove_boundsCheck() {
        Player playerT = new Player(null);
        Player playerB = new Player(null);
        // The x value should always stay in bounds because
        // they are only moving up and down.
        Assert.assertTrue(playerT.getX() < GameState.kCanvasWidth);
        Assert.assertTrue(playerT.getX() > 0);

        Assert.assertTrue(playerT.getY() > 0);
        Assert.assertTrue(playerT.getY() < GameState.kCanvasHeight);

        playerT.move(Direction.UP);
        playerB.move(Direction.DOWN);
        for (int i = 0; i < GameState.kCanvasHeight + 100; i++) {
            playerT.move();
            playerB.move();
        }

        Assert.assertTrue("Expect y >= 0; got y = " + playerT.getY(),
                          playerT.getY() >= 0);
        Assert.assertTrue(String.format("Expect y <= %d; got y = %d",
                                        GameState.kCanvasHeight,
                                        playerB.getY()),
                          playerB.getY() <= GameState.kCanvasHeight);
    }
}