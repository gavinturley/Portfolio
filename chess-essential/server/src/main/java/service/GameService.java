package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.jetbrains.annotations.NotNull;
import server.*;
import java.util.Collection;
import service.ServiceHelp;


/* Implements Service logic dealing with the game board */
public class GameService {
    final private GameDAO gameDAO;
    final private AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public ListGameResult listGames(String authToken) throws DataAccessException {
        ServiceHelp.requireAuth(authDAO, authToken);
        var summaries = gameDAO.listGames().stream().map(GameSummary::from).toList();
        return new ListGameResult(summaries);
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest request) throws DataAccessException {
        ServiceHelp.requireAuth(authDAO, authToken);
        if (ServiceHelp.isBlank(request.gameName())) {
            throw new BadRequestException("Error: bad request");
        }

        var game = new GameData(0, null, null, request.gameName(), new ChessGame());
        int gameID = gameDAO.createGame(game);

        return new CreateGameResult(gameID);
    }

    public void joinGame(String authToken, JoinGameRequest request) throws DataAccessException {
        var auth = ServiceHelp.requireAuth(authDAO, authToken);

        if (!isValidColor(request.playerColor())) {
            throw new BadRequestException("Error: bad request");
        }

        var game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new BadRequestException("Error: bad request");
        }

        GameData updated = getGameData(request, game, auth);

        gameDAO.updateGame(updated);
    }

    @NotNull
    private static GameData getGameData(JoinGameRequest request, GameData game, AuthData auth) throws AlreadyTakenException {
        GameData updated;
        if (request.playerColor().equalsIgnoreCase("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new AlreadyTakenException("Error: already taken");
            }
            updated = game.withWhiteUsername(auth.username());
        } else {
            if (game.blackUsername() != null) {
                throw new AlreadyTakenException("Error: already taken");
            }
            updated = game.withBlackUsername(auth.username());
        }
        return updated;
    }

    private static boolean isValidColor(String color) {
        return "WHITE".equalsIgnoreCase(color) || "BLACK".equalsIgnoreCase(color);
    }
}
