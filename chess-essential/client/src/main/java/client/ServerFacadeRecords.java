package client;

import java.util.Collection;
import model.GameData;

public class ServerFacadeRecords {
    public record RegisterRequest(String username, String password, String email) {}
    public record LoginRequest(String username, String password) {}
    public record CreateGameRequest(String gameName) {}
    public record CreateGameResult(int gameID) {}
    public record ListGameResult(Collection<GameData> games) {}
    public record JoinGameRequest(String playerColor, int gameID) {}
    public record ErrorResponse(String message) {}
}
