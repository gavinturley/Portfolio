package client;

import model.AuthData;
import java.util.Arrays;

public class PreLoginClient {
    private final ServerFacade serverFacade;
    private final Repl repl;

    public PreLoginClient(ServerFacade serverFacade, Repl repl){
        this.serverFacade = serverFacade;
        this.repl = repl;
    }

    public String eval(String input){
        try {
            var tokens = input.trim().split("\\s+");
            var command = tokens.length > 0 ? tokens[0].toLowerCase() : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (command){
                case "login" -> login(params);
                case "register" -> register(params);
                case "quit" -> "quit";
                case "help" -> help();
                default -> "Unknown command. " + help();
            };
        } catch (ResponseException exception) {
            return exception.getMessage();
        } catch (Exception exception) {
            return "Error: unable to process command.";
        }
    }

    private String login(String... params) throws ResponseException {
        if (params.length != 2) {
            return "Expected: login <USERNAME> <PASSWORD>";
        }
        AuthData authData = serverFacade.login(params[0], params[1]);
        signIn(authData);
        return String.format("Signed in as %s.\n", authData.username());
    }

    private String register(String... params) throws ResponseException {
        if (params.length != 3) {
            return "Expected: register <USERNAME> <PASSWORD> <EMAIL>";
        }
        AuthData authData = serverFacade.register(params[0], params[1], params[2]);
        signIn(authData);
        return String.format("Registered and signed in as %s.\n", authData.username());
    }

    private void signIn(AuthData authData){
        repl.setAuthToken(authData.authToken());
        repl.setUsername(authData.username());
        repl.setState(State.SIGNED_IN);
    }

    private String help(){
        return """
                Commands:
                    register <USERNAME> <PASSWORD> <EMAIL> - creates an account
                    login <USERNAME> <PASSWORD> - logs into existing account
                    quit - exits program
                    help - prints this menu
                """;
    }
}
