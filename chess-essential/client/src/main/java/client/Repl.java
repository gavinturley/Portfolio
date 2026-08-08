package client;

import java.util.Scanner;
import static ui.EscapeSequences.RESET_TEXT_COLOR;
import static ui.EscapeSequences.SET_TEXT_COLOR_GREEN;


public class Repl {
    private final PreLoginClient preLoginClient;
    private final PostLoginClient postLoginClient;
    private final GamePlayClient gamePlayClient;

    private State state = State.SIGNED_OUT;
    private String authToken = null;
    private String username = null;

    public Repl(String serverURL) {
        ServerFacade serverFacade = new ServerFacade(serverURL);
        this.preLoginClient = new PreLoginClient(serverFacade, this);
        this.postLoginClient = new PostLoginClient(serverFacade, this);
        this.gamePlayClient = new GamePlayClient(serverFacade, this);
    }

    public void run(){
        System.out.println(SET_TEXT_COLOR_GREEN + "♕ Welcome to 240 Chess. Type Help to get started. ♕" + RESET_TEXT_COLOR);
        System.out.println(preLoginClient.eval("help"));

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")){
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = switch (state) {
                    case SIGNED_OUT -> preLoginClient.eval(line);
                    case SIGNED_IN -> postLoginClient.eval(line);
                    case IN_GAME -> gamePlayClient.eval(line);
                };
                System.out.print(result);
            } catch (Throwable exception) {
                System.out.print("Error: " + exception.getMessage());
            }
        }
        System.out.println();
    }

    public void printPrompt(){
        String label = switch (state) {
            case SIGNED_OUT -> "SIGNED_OUT";
            case SIGNED_IN -> "SIGNED_IN";
            case IN_GAME -> "IN_GAME";
        };
        System.out.print("\n" + RESET_TEXT_COLOR + "[" + label + "] >>> ");
    }

    public GamePlayClient getGamePlayClient() {
        return gamePlayClient;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthToken(){
        return authToken;
    }

    public void setAuthToken(String authToken){
        this.authToken = authToken;
    }

    public State getState(){
        return state;
    }

    public void setState(State state){
        this.state = state;
    }
}
