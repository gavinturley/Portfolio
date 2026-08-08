package client;

import com.google.gson.Gson;
import websocket.WebSocketGson;
import websocket.commands.*;
import websocket.messages.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class WebSocketFacade implements WebSocket.Listener {
    private final WebSocket socket;
    private final Gson gson = WebSocketGson.create();
    private final ServerMessageObserver observer;
    private final StringBuilder buffer = new StringBuilder();

    public interface ServerMessageObserver {
        void notify(ServerMessage message);
    }

    public WebSocketFacade(String url, ServerMessageObserver observer) throws ResponseException {
        this.observer = observer;
        try {
            String wsUrl = url.replace("http://", "ws://").replace("https://", "wss://") + "/ws";
            this.socket = HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .buildAsync(URI.create(wsUrl), this)
                    .join();
        } catch (Exception e) {
            throw new ResponseException(500, "Unable to connect: " + e.getMessage());
        }
    }

    public void connect(String authToken, int gameID) {
        send(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
    }

    public void makeMove(String authToken, int gameID, chess.ChessMove move) {
        send(new MakeMoveCommand(authToken, gameID, move));
    }

    public void leave(String authToken, int gameID) {
        send(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID));
    }

    public void resign(String authToken, int gameID) {
        send(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID));
    }

    private void send(UserGameCommand command) {
        socket.sendText(gson.toJson(command), true);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        buffer.append(data);
        webSocket.request(1);
        if (last) {
            ServerMessage message = gson.fromJson(buffer.toString(), ServerMessage.class);
            buffer.setLength(0);
            observer.notify(message);
        }
        return null;
    }
}