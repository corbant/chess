package client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Map;

import com.google.gson.Gson;

import chess.ChessGame.TeamColor;
import jakarta.websocket.Endpoint;
import jakarta.websocket.Session;
import websocket.commands.UserGameCommand;

public class ServerFacade {
    private final String httpBaseUrl;
    private final String wsUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private WebSocketClient wsClient;
    private final static String AUTH_HEADER_NAME = "authorization";
    private final Gson gson = new Gson();

    public ServerFacade(String hostname, int port) {
        this.httpBaseUrl = String.format("http://%s:%d", hostname, port);
        this.wsUrl = String.format("ws://%s:%d/ws", hostname, port);
    }

    public ServerFacade(String serverUrl, String wsUrl) {
        this.httpBaseUrl = serverUrl;
        this.wsUrl = wsUrl;
    }

    public LoginResponse login(String username, String password)
            throws BadRequestException, UnauthorizedException, ServerErrorException, ConnectionErrorException {
        var body = Map.ofEntries(Map.entry("username", username), Map.entry("password", password));
        var jsonBody = gson.toJson(body);
        var response = post("/session", jsonBody);
        try {
            return parseResponse(response, LoginResponse.class);
        } catch (AlreadyTakenException e) {
            return null;
        }
    }

    public void logout(String authToken) throws UnauthorizedException, ServerErrorException, ConnectionErrorException {
        var response = delete("/session", authToken);
        try {
            parseResponse(response, null);
        } catch (BadRequestException | AlreadyTakenException e) {
        }
    }

    public LoginResponse register(String username, String password, String email)
            throws BadRequestException, AlreadyTakenException, ServerErrorException, ConnectionErrorException {
        var body = Map.ofEntries(Map.entry("username", username), Map.entry("password", password),
                Map.entry("email", email));
        var jsonBody = gson.toJson(body);
        var response = post("/user", jsonBody);
        try {
            return parseResponse(response, LoginResponse.class);
        } catch (UnauthorizedException e) {
            return null;
        }
    }

    public CreateGameResponse createGame(String authToken, String gameName)
            throws UnauthorizedException, BadRequestException, ServerErrorException, ConnectionErrorException {
        var body = Map.ofEntries(Map.entry("gameName", gameName));
        var jsonBody = gson.toJson(body);
        var response = post("/game", authToken, jsonBody);
        try {
            return parseResponse(response, CreateGameResponse.class);
        } catch (AlreadyTakenException e) {
            return null;
        }
    }

    public ListGamesResponse listGames(String authToken)
            throws UnauthorizedException, ServerErrorException, ConnectionErrorException {
        var response = get("/game", authToken);
        try {
            return parseResponse(response, ListGamesResponse.class);
        } catch (BadRequestException | AlreadyTakenException e) {
            return null;
        }
    }

    public void playGame(String authToken, int gameID, TeamColor playerColor, ServerMessageHandler messageHandler)
            throws BadRequestException, UnauthorizedException, AlreadyTakenException, ServerErrorException,
            ConnectionErrorException {
        var body = Map.ofEntries(Map.entry("gameID", gameID), Map.entry("playerColor", playerColor));
        var jsonBody = gson.toJson(body);
        var response = put("/game", authToken, jsonBody);
        parseResponse(response, null);
        try {
            wsClient = new WebSocketClient(wsUrl, messageHandler);
        } catch (Exception e) {
            throw new ConnectionErrorException(e.getMessage());
        }
    }

    public void sendGameCommand(UserGameCommand command) throws ConnectionErrorException {
        if (wsClient == null) {
            throw new ConnectionErrorException("not connected to game");
        }
        try {
            wsClient.sendCommand(command);
        } catch (IOException e) {
            throw new ConnectionErrorException("not connected to game");
        }
    }

    public void clearDB() throws ConnectionErrorException {
        delete("/db");
    }

    private void handleErrorStatusCode(int code, String message)
            throws BadRequestException, UnauthorizedException, AlreadyTakenException, ServerErrorException {
        switch (code) {
            case 400:
                throw new BadRequestException(message);
            case 401:
                throw new UnauthorizedException(message);
            case 403:
                throw new AlreadyTakenException(message);
            case 500:
                throw new ServerErrorException(message);
        }
    }

    private <T> T parseResponse(HttpResponse<String> response, Class<T> responseClass)
            throws BadRequestException, UnauthorizedException, AlreadyTakenException, ServerErrorException {
        if (response.statusCode() != 200) {
            String message = gson.fromJson(response.body(), ServerResponse.class).message();
            handleErrorStatusCode(response.statusCode(), message);
        }
        if (responseClass == null) {
            return null;
        }
        return gson.fromJson(response.body(), responseClass);
    }

    private URI getUri(String path) {
        try {
            return new URI(httpBaseUrl + path);
        } catch (Exception e) {
            return null;
        }
    }

    private HttpResponse<String> get(String path) throws ConnectionErrorException {
        return get(path, null);
    }

    private HttpResponse<String> get(String path, String authToken) throws ConnectionErrorException {
        var requestBuilder = HttpRequest.newBuilder(getUri(path)).GET();
        if (authToken != null) {
            requestBuilder.header(AUTH_HEADER_NAME, authToken);
        }
        var request = requestBuilder.build();
        try {
            return httpClient.send(request, BodyHandlers.ofString());
        } catch (Exception e) {
            throw new ConnectionErrorException(e.getMessage());
        }
    }

    private HttpResponse<String> post(String path, String jsonBody) throws ConnectionErrorException {
        return post(path, null, jsonBody);
    }

    private HttpResponse<String> post(String path, String authToken, String jsonBody) throws ConnectionErrorException {
        var requestBuilder = HttpRequest.newBuilder(getUri(path)).POST(BodyPublishers.ofString(jsonBody));
        if (authToken != null) {
            requestBuilder.header(AUTH_HEADER_NAME, authToken);
        }
        var request = requestBuilder.build();
        try {
            return httpClient.send(request, BodyHandlers.ofString());
        } catch (Exception e) {
            throw new ConnectionErrorException(e.getMessage());
        }
    }

    private HttpResponse<String> put(String path, String jsonBody) throws ConnectionErrorException {
        return put(path, null, jsonBody);
    }

    private HttpResponse<String> put(String path, String authToken, String jsonBody) throws ConnectionErrorException {
        var requestBuilder = HttpRequest.newBuilder(getUri(path)).PUT(BodyPublishers.ofString(jsonBody));
        if (authToken != null) {
            requestBuilder.header(AUTH_HEADER_NAME, authToken);
        }
        var request = requestBuilder.build();
        try {
            return httpClient.send(request, BodyHandlers.ofString());
        } catch (Exception e) {
            throw new ConnectionErrorException(e.getMessage());
        }
    }

    private HttpResponse<String> delete(String path) throws ConnectionErrorException {
        return delete(path, null);
    }

    private HttpResponse<String> delete(String path, String authToken) throws ConnectionErrorException {
        var requestBuilder = HttpRequest.newBuilder(getUri(path)).DELETE();
        if (authToken != null) {
            requestBuilder.header(AUTH_HEADER_NAME, authToken);
        }
        var request = requestBuilder.build();
        try {
            return httpClient.send(request, BodyHandlers.ofString());
        } catch (Exception e) {
            throw new ConnectionErrorException(e.getMessage());
        }
    }
}
