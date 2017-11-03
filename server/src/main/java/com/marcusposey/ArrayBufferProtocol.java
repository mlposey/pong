package com.marcusposey;

import java.nio.ByteBuffer;

/**
 * Transforms objects into messages (byte arrays) that can be read by Javascript
 *
 * Communication between client and server is done using contiguous byte chunks
 * that contain heterogeneous data. On the client (Javascript) side, those
 * chunks are produced/consumed as the ArrayBuffer type. Here, they are handled
 * as a ByteBuffer.
 *
 * Each message must start with an 8-bit directive that indicates its purpose.
 * @see Directive for details.
 */
public class ArrayBufferProtocol {
    /**
     * Creates a message containing the player's id
     *
     * The 6-byte message contains a directive and player id.
     */
    public static byte[] encodeId(final int playerId) {
        // directive = 1 byte, id = 4 bytes
        ByteBuffer message = ByteBuffer.allocate(6);
        message.put((byte)Directive.ID.ordinal());
        message.putInt(playerId);
        return message.array();
    }

    /**
     * Creates a message containing the entire game state
     *
     * The 25-byte message contains a directive, (x, y) coordinate of the first
     * and then second player, and the (x, y) coordinate of the ball.
     */
    public static byte[] encodeGameState(final GameState state) {
        // directive = 1 byte, locations = 24 bytes
        ByteBuffer message = ByteBuffer.allocate(49);
        message.put((byte)Directive.GAME_STATE.ordinal());

        final Entity[] players = state.getPlayers();
        message.putInt(players[0].getX());
        message.putInt(players[0].getY());
        message.putInt(players[1].getX());
        message.putInt(players[1].getY());

        final Ball ball = state.getBall();
        message.putInt(ball.getX());
        message.putInt(ball.getY());

        return message.array();
    }

    /**
     * Creates a message that holds a game's score
     *
     * The 9-byte message contains a directive and score for player 0 and 1.
     */
    public static byte[] encodeGameScore(final int[] scores) {
        ByteBuffer message = ByteBuffer.allocate(9);
        message.put((byte)Directive.GAME_SCORE.ordinal());
        message.putInt(scores[0]);
        message.putInt(scores[1]);
        return message.array();
    }

    // Todo: Split game state into only the changed components.
    // Sending the entire game state may not be optimal.
}
