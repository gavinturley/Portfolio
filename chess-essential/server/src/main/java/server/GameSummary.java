package server;

import model.GameData;

public record GameSummary(int gameID, String whiteUsername, String blackUsername, String gameName) {
    public static GameSummary from(GameData game){
        return new GameSummary(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName());
    }
}