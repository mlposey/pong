package com.marcusposey;

import org.java_websocket.WebSocket;

import java.util.Random;

/** A user-controllable entity that represents a paddle in a game */
public class Player extends Entity {
    public enum Direction {UP, DOWN}

    // How fast the player moves
    private static final int kSpeed = 2;

    private final int id;
    // A connection to the client controller
    private WebSocket webSocket;

    // True if the player is moving up or down; false if it is still
    private boolean isMoving = false;
    // The direction in which the player is moving
    private Direction currentDirection;

    /** Creates a new player bound to an open Websocket */
    public Player(WebSocket webSocket) {
        super(20, 100, 20, 100);
        this.webSocket = webSocket;
        id = new Random().nextInt(Integer.MAX_VALUE);
    }

    /**
     * Returns the player's Id
     * This is a random integer in [0, Integer.MAX_VALUE].
     */
    public int getId() {
        return id;
    }

    /**
     * Returns a (hopefully) active Websocket to the player
     * The player could disconnect after calling this, so ensure the exception
     * WebsocketNotConnectedException is caught when using the socket.
     */
    public WebSocket getSocket() {
        return webSocket;
    }

    /** Moves a player one unit up or down in the y axis */
    public void move(final Direction direction) {
        final int offset = direction == Direction.UP ? -kSpeed : kSpeed;
        final int newY = getY() + offset;
        // Ensure new location is within the bounds of the canvas.
        setY(Math.max(Math.min(newY, GameState.kCanvasHeight - getHeight()), 0));
        currentDirection = direction;
        isMoving = true;
    }

    /** Signals to the player to stop moving */
    public void stopMoving() {
        isMoving = false;
    }

    /**
     * Moves the player according to the last movement request
     *
     * If stopMoving() was last called, the player will not move. It will otherwise
     * move in the direction last supplied to move(Direction).
     */
    public void move() {
        if (isMoving) {
            move(currentDirection);
        }
    }
}
