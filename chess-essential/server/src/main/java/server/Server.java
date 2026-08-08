package server;

import dataaccess.*;
import io.javalin.websocket.WsMessageContext;
import server.websocket.WebSocketHandler;
import service.*;
import io.javalin.*;
import io.javalin.http.Context;
import com.google.gson.Gson;

import java.util.Map;

public class Server {
    private final Javalin javalin;
    private final Gson gson = new Gson();

    private final ClearService clearService;
    private final GameService gameService;
    private final UserService userService;

    /* Server logic and memory management */
    public Server() {
        javalin = Javalin.create()
                .get("/echo/{msg}", ctx -> ctx.result("HTTP response: " + ctx.pathParam("msg")));

        UserDAO userDAO;
        GameDAO gameDAO;
        AuthDAO authDAO;

        try {
            userDAO = new SqlUserDAO();
            gameDAO = new SqlGameDAO();
            authDAO = new SqlAuthDAO();
        } catch (DataAccessException exception) {
            throw new RuntimeException(exception);
        }
        clearService = new ClearService(gameDAO, userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        userService = new UserService(userDAO, authDAO);

        javalin.post("/user", this::register);
        javalin.post("/session", this::login);
        javalin.delete("/session", this::logout);
        javalin.get("/game", this::listGames);
        javalin.post("/game", this::createGame);
        javalin.put("/game", this::joinGame);
        javalin.delete("/db", this::clear);

        var webSocketHandler = new WebSocketHandler(authDAO, gameDAO);
        javalin.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                ctx.enableAutomaticPings();
                System.out.println("Websocket connected");
            });
            ws.onMessage(ctx -> {
                webSocketHandler.onMessage(ctx, ctx.message());
            });
            ws.onClose(_ -> System.out.println("Websocket closed"));
        });

        javalin.exception(Exception.class, (e, ctx) -> sendBody(ctx, 500, Map.of("message", "Error: " + e.getMessage())));
    }


    /* The functions below deal with Handlers, sending the date to the Service for logic applications */

    private void joinGame(Context ctx) {
        try {
            var request = gson.fromJson(ctx.body(), JoinGameRequest.class);
            gameService.joinGame(ctx.header("authorization"), request);
            sendBody(ctx, 200, Map.of());
        } catch (BadRequestException e) {
            sendError(ctx, 400, "bad request");
        } catch (UnauthorizedException e) {
            sendError(ctx, 401, "unauthorized");
        } catch (AlreadyTakenException e) {
            sendError(ctx, 403, "already taken");
        } catch (DataAccessException e) {
            sendError(ctx, 500, e.getMessage());
        }
    }

    private void createGame(Context ctx) {
        try {
            var request = gson.fromJson(ctx.body(), CreateGameRequest.class);
            var result = gameService.createGame(ctx.header("authorization"), request);
            sendBody(ctx, 200, result);
        } catch (BadRequestException e) {
            sendError(ctx, 400, "bad request");
        } catch (UnauthorizedException e) {
            sendError(ctx, 401, "unauthorized");
        } catch (DataAccessException e) {
            sendError(ctx, 500, e.getMessage());
        }
    }
    
    private void listGames(Context ctx) {
        try {
            var result = gameService.listGames(ctx.header("authorization"));
            sendBody(ctx, 200, result);
        } catch (UnauthorizedException e) {
            sendError(ctx, 401, "unauthorized");
        } catch (DataAccessException e) {
            sendError(ctx, 500, e.getMessage());
        }
    }

    private void logout(Context ctx) {
        try {
            userService.logout(ctx.header("authorization"));
            sendBody(ctx, 200, Map.of());
        } catch (UnauthorizedException e) {
            sendError(ctx, 401, "unauthorized");
        } catch (DataAccessException e) {
            sendError(ctx, 500, e.getMessage());
        }
    }

    private void login(Context ctx) {
        try {
            var request = gson.fromJson(ctx.body(), LoginRequest.class);
            var result = userService.login(request);
            sendBody(ctx, 200, result);
        } catch (BadRequestException e) {
            sendError(ctx, 400, "bad request");
        } catch (UnauthorizedException e) {
            sendError(ctx, 401, "unauthorized");
        } catch (DataAccessException e) {
            sendError(ctx, 500, e.getMessage());
        }
    }

    private void register(Context ctx) {
        try {
            var request = gson.fromJson(ctx.body(), RegisterRequest.class);
            var result = userService.register(request);
            sendBody(ctx, 200, result);
        } catch (BadRequestException e) {
            sendError(ctx, 400, "bad request");
        } catch (AlreadyTakenException e) {
            sendError(ctx, 403, "already taken");
        } catch (DataAccessException e) {
            sendError(ctx, 500, e.getMessage());
        }
    }

    private void clear(Context ctx) {
        try {
            clearService.clear();
            sendBody(ctx, 200, Map.of());
        } catch (DataAccessException e) {
            sendError(ctx, 500, e.getMessage());
        }
    }


    /* Helper functions */

    private void sendError(Context ctx, int status, String message) {
        sendBody(ctx, status, Map.of("message", "Error: " + message));
    }

    private void sendBody(Context ctx, int status, Object body) {
        ctx.status(status);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(body));
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
