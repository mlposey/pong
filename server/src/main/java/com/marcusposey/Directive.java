package com.marcusposey;

/**
 * Directives indicate the purpose of a ByteBuffer message.
 *
 * The protocol states they should be the first object in a message.
 */
public enum Directive {
    ID, // 0
    GAME_STATE, // 1
    MOVE_PLAYER_UP, // 2
    MOVE_PLAYER_DOWN, // 3
    STOP_MOVING_PLAYER, // 4
    GAME_SCORE // 5
}
