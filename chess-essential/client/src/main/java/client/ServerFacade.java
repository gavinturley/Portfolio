package client;

import com.google.gson.Gson;
import model.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Collection;


public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public AuthData register(String username, String password, String email) throws ResponseException {
        var request = new UserData(username, password, email);
        return this.makeRequest("POST", "/user", null, request, AuthData.class);
    }

    public AuthData login(String username, String password) throws ResponseException {
        record LoginRequest(String username, String password) {}
        var request = new LoginRequest(username, password);
        return this.makeRequest("POST", "/session", null, request, AuthData.class);
    }

    public void logout(String authToken) throws ResponseException {
        this.makeRequest("DELETE", "/session", authToken, null, null);
    }

    public Collection<GameData> listGames(String authToken) throws ResponseException {
        record ListGamesResult(Collection<GameData> games) {}
        var result = this.makeRequest("GET", "/game", authToken, null, ListGamesResult.class);
        return result.games();
    }

    public int createGame(String authToken, String gameName) throws ResponseException {
        record CreateGameRequest(String gameName) {}
        record CreateGameResult(int gameID) {}
        var request = new CreateGameRequest(gameName);
        var result = this.makeRequest("POST", "/game", authToken, request, CreateGameResult.class);
        return result.gameID();
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws ResponseException {
        record JoinGameRequest(String playerColor, int gameID) {}
        var request = new JoinGameRequest(playerColor, gameID);
        this.makeRequest("PUT", "/game", authToken, request, null);
    }

    public void clear() throws ResponseException {
        this.makeRequest("DELETE", "/db", null, null, null);
    }

    private <T> T makeRequest(String method, String path, String authToken, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URI uri = new URI(serverUrl + path);
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (authToken != null) {
                http.addRequestProperty("Authorization", authToken);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException exception) {
            throw exception;
        } catch (URISyntaxException | IOException exception) {
            throw new ResponseException(500, "Error: failed to connect to server");
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        int status = http.getResponseCode();
        if (status != 200) {
            String message = "Error: an unknown error occurred";
            try (InputStream responseError = http.getErrorStream()) {
                if (responseError != null) {
                    var errorMap = new Gson().fromJson(new InputStreamReader(responseError), Map.class);
                    if (errorMap != null && errorMap.get("message") != null){
                        message = errorMap.get("message").toString();
                    }
                }
            }
            throw new ResponseException(status, message);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        try (InputStream responseBody = http.getInputStream()) {
            InputStreamReader reader = new InputStreamReader(responseBody);
            if (responseClass != null) {
                response = new Gson().fromJson(reader, responseClass);
            }
        }
        return response;
    }
}
