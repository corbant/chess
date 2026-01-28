package client;

import java.io.IOException;
import java.net.URI;

import com.google.gson.Gson;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

public class WebSocketClient extends Endpoint {

    Session session;
    Gson gson;

    public WebSocketClient(String url, ServerMessageHandler messageHandler) throws Exception {
        URI uri = new URI(url);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

                switch (serverMessage.getServerMessageType()) {
                    case ERROR:
                        serverMessage = gson.fromJson(message, ErrorMessage.class);
                        break;
                    case LOAD_GAME:
                        serverMessage = gson.fromJson(message, LoadGameMessage.class);
                        break;
                    case NOTIFICATION:
                        serverMessage = gson.fromJson(message, NotificationMessage.class);
                        break;

                }

                messageHandler.handleMessage(serverMessage);
            }

        });
    }

    public void sendCommand(UserGameCommand command) throws IOException {
        String json = gson.toJson(command);

        session.getBasicRemote().sendText(json);
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
    }

}
