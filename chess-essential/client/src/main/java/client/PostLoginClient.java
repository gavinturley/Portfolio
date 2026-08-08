package client;

import chess.ChessGame;
import model.GameData;
import ui.BoardPrinter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class PostLoginClient {
    private final ServerFacade serverFacade;
    private final Repl repl;
    private final Map<Integer, GameData> gameList = new LinkedHashMap<>();

    public PostLoginClient(ServerFacade serverFacade, Repl repl){
        this.serverFacade = serverFacade;
        this.repl = repl;
    }

    public String eval(String input) {
        try {
            var tokens = input.trim().split("\\s+");
            var command = tokens.length > 0 ? tokens[0].toLowerCase() : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (command) {
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "play" -> playGame(params);
                case "observe" -> observeGame(params);
                case "help" -> help();
                default -> "Unknown command. " + help();
            };
        } catch (ResponseException exception){
            return exception.getMessage();
        } catch (Exception exception){
            return "Error: unable to process command.";
        }
    }

    private String logout() throws ResponseException {
        serverFacade.logout(repl.getAuthToken());
        repl.setUsername(null);
        repl.setAuthToken(null);
        repl.setState(State.SIGNED_OUT);
        gameList.clear();
        return "Logged out.\n";
    }

    private String createGame(String... params) throws ResponseException {
        if (params.length != 1) {
            return "Expected create <GAME NAME>";
        }
        serverFacade.createGame(repl.getAuthToken(), params[0]);
        return String.format("Created game '%s'.\n", params[0]);
    }

    private String listGames() throws ResponseException {
        var games = serverFacade.listGames(repl.getAuthToken());
        gameList.clear();
        if (games.isEmpty()){
            return "No games created. Use 'create <GAME NAME> to create a game.\n";
        }

        StringBuilder stringBuilder = new StringBuilder();
        int i = 1;
        for (var game : games){
            gameList.put(i, game);
            String white = game.whiteUsername() == null ? "--" : game.whiteUsername();
            String black = game.blackUsername() == null ? "--" : game.blackUsername();
            stringBuilder.append(String.format("%d. %s (White: %s, Black: %s)%n", i, game.gameName(), white, black));
            i++;
        }
        return stringBuilder.toString();
    }

    private String playGame(String... params) throws ResponseException {
        if (params.length != 2){
            return "Expected: play <GAME NUMBER> <WHITE|BLACK>";
        }

        GameData game = getGame(params[0]);
        if (game == null){
            return "Error: no such game number. Use 'list' to see available games or 'create <GAME NAME>' to create one.";
        }

        String color = params[1].toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK")){
            return "Expected: play <GAME NAME> <WHITE|BLACK>";
        }

        serverFacade.joinGame(repl.getAuthToken(), color, game.gameID());
        ChessGame.TeamColor perspective = color.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

        repl.setState(State.IN_GAME);
        repl.getGamePlayClient().start(game.gameID(), perspective);
        return "";
    }

    private String observeGame(String... params) throws ResponseException {
        if (params.length != 1){
            return "Expected: observe <GAME NUMBER>";
        }

        GameData game = getGame(params[0]);
        if (game == null) {
            return "Error: no such game number. Use 'list' to see available games.";
        }

        repl.setState(State.IN_GAME);
        repl.getGamePlayClient().start(game.gameID(), null);
        return "";
    }

    private GameData getGame(String numberString) {
        try {
            int number = Integer.parseInt(numberString);
            return gameList.get(number);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String help() {
        return """
                Commands:
                  create <GAME NAME> - create a new game
                  list - list all existing games
                  play <GAME NUMBER> <WHITE|BLACK> - join a game as a player
                  observe <GAME NUMBER> - observe a game
                  logout - log out of your account
                  help - display this help text
                """;
    }
}
