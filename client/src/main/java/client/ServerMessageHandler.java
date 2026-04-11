package client;

import websocket.messages.ServerMessage;

public interface ServerMessageHandler {
    void handleMessage(ServerMessage message);
}
