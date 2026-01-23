package client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Map;

import com.google.gson.Gson;

public class ServerFacade {
    private final String baseUrl;
    private final HttpClient client = HttpClient.newHttpClient();
    private final static String AUTH_HEADER_NAME = "authorization";
    private final Gson gson = new Gson();

    public ServerFacade(int hostname, int port) {
        this.baseUrl = String.format("http://%s:%d", hostname, port);
    }

    public ServerFacade(String serverUrl) {
        this.baseUrl = serverUrl;
    }

    public LoginResponse login(String username, String password)
            throws BadRequestException, UnauthorizedException, ServerErrorException {
        var body = Map.ofEntries(Map.entry("username", username), Map.entry("password", password));
        var jsonBody = gson.toJson(body);
        var response = post("/session", jsonBody);
        if (response.statusCode() != 200) {
            String message = gson.fromJson(response.body(), ServerMessage.class).message();
            switch (response.statusCode()) {
                case 400:
                    throw new BadRequestException(message);
                case 401:
                    throw new UnauthorizedException(message);
                case 500:
                    throw new ServerErrorException(message);
            }
        }
        return gson.fromJson(response.body(), LoginResponse.class);
    }

    public void logout(String authToken) throws UnauthorizedException, ServerErrorException {
        var response = delete("/session", authToken);
    }

    public void register(String username, String password, String email)
            throws BadRequestException, AlreadyTakenException, ServerErrorException {
        var body = Map.ofEntries(Map.entry("username", username), Map.entry("password", password),
                Map.entry("email", email));
        var jsonBody = gson.toJson(body);
        var response = post("/user", jsonBody);
    }

    public void createGame(String authToken, String gameName)
            throws UnauthorizedException, BadRequestException, ServerErrorException {
        var body = Map.ofEntries(Map.entry("gameName", gameName));
        var jsonBody = gson.toJson(body);
        var response = post("/game", authToken, jsonBody);
    }

    public void listGames(String authToken) throws UnauthorizedException, ServerErrorException {
        var response = get("/game", authToken);
    }

    public void playGame(String authToken, int gameID, String playerColor)
            throws BadRequestException, UnauthorizedException, AlreadyTakenException, ServerErrorException {
        var body = Map.ofEntries(Map.entry("gameID", gameID), Map.entry("playerColor", playerColor));
        var jsonBody = gson.toJson(body);
        var response = put("/game", authToken, jsonBody);
    }

    public void observeGame(int gameID) {

    }

    private URI getUri(String path) {
        try {
            return new URI(baseUrl + "/session");
        } catch (Exception e) {
            return null;
        }
    }

    private HttpResponse<String> get(String path) {
        return get(path, null);
    }

    private HttpResponse<String> get(String path, String authToken) {
        var requestBuilder = HttpRequest.newBuilder(getUri(path)).GET();
        if (authToken != null) {
            requestBuilder.header(AUTH_HEADER_NAME, authToken);
        }
        var request = requestBuilder.build();
        return client.send(request, BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String jsonBody) {
        return post(path, null, jsonBody);
    }

    private HttpResponse<String> post(String path, String authToken, String jsonBody) {
        var requestBuilder = HttpRequest.newBuilder(getUri(path)).POST(BodyPublishers.ofString(jsonBody));
        if (authToken != null) {
            requestBuilder.header(AUTH_HEADER_NAME, authToken);
        }
        var request = requestBuilder.build();
        return client.send(request, BodyHandlers.ofString());
    }

    private HttpResponse<String> put(String path, String jsonBody) {
        return put(path, null, jsonBody);
    }

    private HttpResponse<String> put(String path, String authToken, String jsonBody) {
        var requestBuilder = HttpRequest.newBuilder(getUri(path)).PUT(BodyPublishers.ofString(jsonBody));
        if (authToken != null) {
            requestBuilder.header(AUTH_HEADER_NAME, authToken);
        }
        var request = requestBuilder.build();
        return client.send(request, BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(String path) {
        return delete(path, null);
    }

    private HttpResponse<String> delete(String path, String authToken) {
        var requestBuilder = HttpRequest.newBuilder(getUri(path)).DELETE();
        if (authToken != null) {
            requestBuilder.header(AUTH_HEADER_NAME, authToken);
        }
        var request = requestBuilder.build();
        return client.send(request, BodyHandlers.ofString());
    }
}
