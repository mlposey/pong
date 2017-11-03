package com.marcusposey;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * World holds the state of a collection of players and their matches.
 *
 * A World exists within a game loop that can be started with beginLoop() and
 * permanently ended with nuke().
 */
public class World {
    // A map of all player's id to their object
    private ConcurrentHashMap<Integer, Player> players = new ConcurrentHashMap<>();

    // All players who are not in a match
    private Queue<Player> lobby = new LinkedList<>();
    // Lock for the lobby queue
    private ReentrantLock lobbyGuard = new ReentrantLock();

    // A mapping from a player's id to their active match
    private Map<Integer, GameState> matches = new HashMap<>();

    // True if the game loop is running
    private boolean isActive = false;

    /** Starts the game loop in another thread */
    public synchronized void beginLoop() {
        if (!isActive) isActive = true;

        new Thread(() -> {
            while (isActive) {
                removeDisconnectedPlayers();
                createMatches();
                matches.values().parallelStream().forEach(GameState::update);

                try { Thread.sleep(12); }
                catch (InterruptedException e) {Thread.currentThread().interrupt();}
            }
        }).start();
    }

    /** Performs a permanent, graceful teardown of the world state */
    public void nuke() {
        if (!isActive) return;
        // todo: broadcast message
        // todo: close active connections
        isActive = false;
    }

    /** Creates and adds a player to the world, returning their unique id */
    public synchronized int addPlayer(WebSocket webSocket) {
        Player player = null;
        while (player == null) {
            // todo: Find a better way to get unique Id's.
            player = new Player(webSocket);
            if (players.containsKey(player.getId())) {
                player = null;
            }
        }
        players.put(player.getId(), player);

        lobbyGuard.lock();
        lobby.add(player);
        lobbyGuard.unlock();

        return player.getId();
    }

    /**
     * Sends a movement signal to the player to go in a specific direction
     * The player will continue moving until stopMovingPlayer(int) is called.
     */
    public void movePlayer(final int playerId, final Player.Direction direction) {
        final Player player = players.get(playerId);
        if (player == null) return;
        player.move(direction);
    }

    /** Makes the player stop moving */
    public void stopMovingPlayer(final int playerId) {
        final Player player = players.get(playerId);
        if (player == null) return;
        player.stopMoving();
    }

    /** Returns true if the WebSocket belongs to the player; false otherwise */
    public synchronized boolean isSenderAuthentic(final WebSocket ws, int playerId) {
        final Player player = players.get(playerId);
        return player != null && player.getSocket() == ws;
    }

    /** Creates matches for players that are waiting in the lobby */
    private void createMatches() {
        lobbyGuard.lock();
        while (lobby.size() > 1) {
            createMatch(lobby.poll(), lobby.poll());
        }
        lobbyGuard.unlock();
    }

    /**
     * Attempts to create a match between two players
     *
     * While this almost always results in a valid match, either player's
     * connection could drop during the creation process, throwing the
     * widowed opponent back into the lobby.
     */
    private synchronized void createMatch(Player a, Player b) {
        GameState state = new GameState(a, b);
        matches.put(a.getId(), state);
        matches.put(b.getId(), state);

        final byte[] stateMessage = ArrayBufferProtocol.encodeGameState(state);
        try {
            a.getSocket().send(stateMessage);
            b.getSocket().send(stateMessage);
            System.out.println("New match between " + a.getId() + " and "
                               + b.getId());
        } catch (WebsocketNotConnectedException e) {
            // Bad timing, eh?
            if (a.getSocket().isOpen()) lobby.add(a);
            else lobby.add(b);
        }
    }

    /**
     * Removes references to players that have disconnected
     *
     * Opponents of disconnected players are moved to the front of the lobby
     * queue and instantly put into a game if other players are waiting.
     */
    private void removeDisconnectedPlayers() {
        for (int playerId : players.keySet()) {
            Player player = players.get(playerId);
            WebSocket playerSocket = player.getSocket();

            if (playerSocket.getReadyState() == WebSocket.READYSTATE.CLOSED) {
                if (matches.containsKey(playerId)) {
                    Player opponent = matches.get(playerId).getOther(player);
                    matches.remove(playerId);
                    matches.remove(opponent.getId());

                    // Put the disconnected player's opponent in a new game if
                    // someone is waiting in the lobby.
                    lobbyGuard.lock();
                    Player lobbyPlayer;
                    if (!lobby.isEmpty()) {
                        lobbyPlayer = lobby.poll();
                        createMatch(opponent, lobbyPlayer);
                    } else {
                        lobby.add(opponent);
                    }
                    lobbyGuard.unlock();
                } else {
                    lobbyGuard.lock();
                    lobby.remove(player);
                    lobbyGuard.unlock();
                }

                // Todo: Signal to the opponent that they left.
                players.remove(playerId);
                System.out.println("Removed " + playerId);
            } else {
                try {
                    playerSocket.sendPing();
                } catch (WebsocketNotConnectedException e) {}
            }
        }
    }
}
