package server.websocket;

import io.javalin.websocket.WsContext;

public record Connection(String authToken, int gameID, WsContext session) {
}
