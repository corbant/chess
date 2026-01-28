package server;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.javalin.websocket.WsContext;
import websocket.messages.ServerMessage;

public class WebsocketConnectionManager {
    private final ConcurrentHashMap<Integer, Set<WsContext>> connections = new ConcurrentHashMap<>();

    public void addSession(int gameID, WsContext session) {
        connections.computeIfAbsent(gameID, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void removeSession(int gameID, WsContext session) {
        Set<WsContext> sessions = connections.get(gameID);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                connections.remove(gameID);
            }
        }
    }

    public void broadcast(int gameID, Object message, WsContext exclude) {
        var sessions = connections.get(gameID);
        if (sessions != null) {
            for (var session : sessions) {
                if (session != exclude && session.session.isOpen()) {
                    session.sendAsClass(message, message.getClass());
                }
            }
        }
    }

    public void broadcastAll(int gameID, Object message) {
        broadcast(gameID, message, null);
    }

}
