package service.result;

import websocket.messages.ServerMessage;

public record OutboundWSServerMessage(Target target, ServerMessage message) {
    public enum Target {
        SELF, OTHERS, ALL
    }
}
