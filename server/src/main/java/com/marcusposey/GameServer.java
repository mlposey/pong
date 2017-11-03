package com.marcusposey;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * A WebSocket server that assigns players unique id's and accepts requests from
 * them to move their character. Communication here, and throughout other portions
 * of code, is done through the spec defined in ArrayBufferProtocol.arra
 */
public class GameServer extends WebSocketServer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);
    private final Directive[] directives = Directive.values();
    private World world;

    /**
     * Creates a new game server awaiting communication
     * @param port the port on which to listen. This will be reused.
     * @param world an initialized world that has already started its game loop
     */
    public GameServer(final int port, World world) {
        super(new InetSocketAddress(port));
        setReuseAddr(true);
        this.world = world;
    }

    /** Creates a new player and responds to the client with their unique id */
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        final int playerId = world.addPlayer(webSocket);
        webSocket.send(ArrayBufferProtocol.encodeId(playerId));
    }

    public void onClose(WebSocket webSocket, int i, String s, boolean b) { }

    public void onMessage(WebSocket webSocket, String s) { }

    @Override
    public void onMessage(WebSocket webSocket, ByteBuffer message) {
        // Use a thread pool, as per:
        // https://github.com/TooTallNate/Java-WebSocket/issues/517
        final int dir = message.get();
        final int playerId  = message.getInt();
        if (!world.isSenderAuthentic(webSocket, playerId)) {
            // Todo: Use enum for error codes.
            webSocket.close(1, "Connection closed; unauthentic request");
            return;
        }

        switch (directives[dir]) {
            case MOVE_PLAYER_UP:
                world.movePlayer(playerId, Player.Direction.UP);
                break;
            case MOVE_PLAYER_DOWN:
                world.movePlayer(playerId, Player.Direction.DOWN);
                break;
            case STOP_MOVING_PLAYER:
                world.stopMovingPlayer(playerId);
                break;
            default:
                webSocket.send(String.valueOf(dir) + " is not a valid directive");
        }
    }

    public void onError(WebSocket webSocket, Exception e) { }

    public void onStart() {
        log.info("Server has started.");
        System.out.println("Type exit for a graceful shutdown.");
    }
}
