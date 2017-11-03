package com.marcusposey;

import org.java_websocket.exceptions.WebsocketNotConnectedException;

public class GameState {
    public static final int kCanvasWidth = 600;
    public static final int kCanvasHeight = 400;
    private static final int kPlayerSpeed = 7;

    // The two players which manipulate the game state
    private final Player[] players;
    private final int scores[];

    // The game ball
    private Ball ball = new Ball();

    /** Creates a new state that two players will share */
    public GameState(Player a, Player b) {
        players = new Player[]{a, b};
        scores = new int[players.length];
        setDefaultLocations();
    }

    /** Modifies state according to game rules, not user input */
    public void update() {
        players[0].move();
        players[1].move();
        final boolean didPlayerWin = ball.move(players);

        final byte[] message;

        if (didPlayerWin) {
            final int winner = ball.getLastToHit();
            scores[winner]++;
            message = ArrayBufferProtocol.encodeGameScore(scores);
            // Todo: The ball was already reset, so they should also get the state.
        } else {
            message = ArrayBufferProtocol.encodeGameState(this);
        }

        try {
            players[0].getSocket().send(message);
            players[1].getSocket().send(message);
        } catch (WebsocketNotConnectedException e) {
            // todo: ensure prune process removes the missing person.
        }
    }

    /** Returns the player which is not a */
    public Player getOther(final Player a) {
        return a.getId() == players[0].getId() ? players[1] : players[0];
    }

    /** Returns an array containing the two players in the match */
    public Player[] getPlayers() {
        return players;
    }

    /** Returns the (x, y) coordinate of the ball */
    public Ball getBall() {
        return ball;
    }

    /** Returns the score of the player */
    public int getScore(final Player a) {
        return scores[a.getId() == players[0].getId() ? 0 : 1];
    }

    /** Sets the default coordinates of the players */
    private void setDefaultLocations() {
        final int w = 20;
        players[0].setX(w);
        players[0].setY((kCanvasHeight - players[0].getHeight()) / 2);
        players[1].setX(kCanvasWidth - 2  * w);
        players[1].setY((kCanvasHeight - players[0].getHeight()) / 2);
    }
}
