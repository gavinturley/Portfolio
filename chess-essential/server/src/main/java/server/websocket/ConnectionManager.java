package server.websocket;

import com.google.gson.Gson;
import websocket.messages.ServerMessage;
import java.util.concurrent.ConcurrentHashMap;
import io.javalin.websocket.WsContext;

public class ConnectionManager {
    private final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public void add(String authToken, int gameID, WsContext session) {
        connections.put(authToken, new Connection(authToken, gameID, session));
    }

    public void remove(String authToken) {
        connections.remove(authToken);
    }

    public void broadcast(int gameID, String excludeAuthToken, ServerMessage message) {
        var json = gson.toJson(message);
        for (var connection : connections.values()) {
            if (connection.gameID() == gameID && !connection.authToken().equals(excludeAuthToken)) {
                if (connection.session().session.isOpen()) {
                    connection.session().send(json);
                }
            }
        }
    }
}
