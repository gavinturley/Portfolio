package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.*;
import model.GameData;
import websocket.WebSocketGson;
import websocket.commands.*;
import websocket.messages.*;
import io.javalin.websocket.WsContext;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

public class WebSocketHandler {
    private final Gson gson = WebSocketGson.create();
    private final ConnectionManager connections = new ConnectionManager();
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final Set<Integer> gameOverGames = ConcurrentHashMap.newKeySet();

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void onMessage(WsContext context, String message) {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(context, command);
                case MAKE_MOVE -> handleMakeMove(context, (MakeMoveCommand) gson.fromJson(message, MakeMoveCommand.class));
                case LEAVE -> handleLeave(context, command);
                case RESIGN -> handleResign(context, command);
            }
        } catch (Exception exception) {
            context.send(gson.toJson(new ErrorMessage("Error: " + exception.getMessage())));
        }
    }

    private void handleConnect(WsContext context, UserGameCommand command) throws DataAccessException {
        var auth = authDAO.getAuth(command.getAuthToken());
        if (auth == null) {
            context.send(gson.toJson(new ErrorMessage("Error: unauthorized")));
            return;
        }
        GameData game = gameDAO.getGame(command.getGameID());
        if (game == null) {
            context.send(gson.toJson(new ErrorMessage("Error: bad game ID")));
            return;
        }

        connections.add(command.getAuthToken(), command.getGameID(), context);
        context.send(gson.toJson(new LoadGameMessage(game.game())));
        String role = auth.username().equals(game.whiteUsername()) ? "white"
                : auth.username().equals(game.blackUsername()) ? "black" : "observer";
        String note = "%s connected as %s".formatted(auth.username(), role);
        connections.broadcast(command.getGameID(), command.getAuthToken(), new NotificationMessage(note));
    }

    private void handleMakeMove(WsContext context, MakeMoveCommand command) throws DataAccessException {
        var auth = authDAO.getAuth(command.getAuthToken());
        if (auth == null) {
            context.send(gson.toJson(new ErrorMessage("Error: unauthorized")));
            return;
        }

        GameData game = gameDAO.getGame(command.getGameID());
        if (game == null) {
            context.send(gson.toJson(new ErrorMessage("Error: bad game ID")));
            return;
        }

        if (gameOverGames.contains(command.getGameID())) {
            context.send(gson.toJson(new ErrorMessage("Error: game is over")));
            return;
        }

        boolean isWhite = auth.username().equals(game.whiteUsername());
        boolean isBlack = auth.username().equals(game.blackUsername());
        if (!isWhite && !isBlack) {
            context.send(gson.toJson(new ErrorMessage("Error: observers cannot make moves")));
            return;
        }

        var myColor = isWhite ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        if (game.game().getTeamTurn() != myColor) {
            context.send(gson.toJson(new ErrorMessage("Error: not your turn")));
            return;
        }

        try {
            game.game().makeMove(command.getMove());
        } catch (chess.InvalidMoveException exception) {
            context.send(gson.toJson(new ErrorMessage("Error: invalid move")));
            return;
        }

        gameDAO.updateGame(game);
        connections.broadcast(command.getGameID(), null, new LoadGameMessage(game.game()));

        String note = auth.username() + " moved " + command.getMove();
        connections.broadcast(command.getGameID(), command.getAuthToken(), new NotificationMessage(note));

        var opponentColor = myColor == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

        if (game.game().isInCheckmate(opponentColor)) {
            gameOverGames.add(command.getGameID());
            connections.broadcast(command.getGameID(), null, new NotificationMessage(opponentColor + " is in checkmate. Game over."));
        } else if (game.game().isInStalemate(opponentColor)) {
            gameOverGames.add(command.getGameID());
            connections.broadcast(command.getGameID(), null, new NotificationMessage("Stalemate. Game over."));
        } else if (game.game().isInCheck(opponentColor)) {
            connections.broadcast(command.getGameID(), null, new NotificationMessage(opponentColor + " is in check."));
        }
    }

    private void handleLeave(WsContext context, UserGameCommand command) throws DataAccessException {
        var auth = authDAO.getAuth(command.getAuthToken());
        if (auth == null) {
            context.send(gson.toJson(new ErrorMessage("Error unauthorized")));
            return;
        }

        GameData game = gameDAO.getGame(command.getGameID());
        if (game != null) {
            if (auth.username().equals(game.whiteUsername())) {
                gameDAO.updateGame(game.withWhiteUsername(null));
            } else if (auth.username().equals(game.blackUsername())) {
                gameDAO.updateGame(game.withBlackUsername(null));
            }
        }

        connections.broadcast(command.getGameID(), command.getAuthToken(), new NotificationMessage(auth.username() + " left the game"));
        connections.remove(command.getAuthToken());
    }

    private void handleResign(WsContext context, UserGameCommand command) throws DataAccessException {
        var auth = authDAO.getAuth(command.getAuthToken());
        if (auth == null) {
            context.send(gson.toJson(new ErrorMessage("Error unauthorized")));
            return;
        }

        GameData game = gameDAO.getGame(command.getGameID());
        if (game == null) {
            context.send(gson.toJson(new ErrorMessage("Error: bad game ID")));
            return;
        }

        if (gameOverGames.contains(command.getGameID())) {
            context.send(gson.toJson(new ErrorMessage("Error: game already over")));
            return;
        }

        boolean isPlayer = auth.username().equals(game.whiteUsername()) || auth.username().equals(game.blackUsername());
        if (!isPlayer) {
            context.send(gson.toJson(new ErrorMessage("Error: observers cannot resign")));
            return;
        }

        gameOverGames.add(command.getGameID());
        connections.broadcast(command.getGameID(), null, new NotificationMessage(auth.username() + " resigned"));
    }
}
