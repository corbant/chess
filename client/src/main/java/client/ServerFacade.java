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
    private final String BASE_URL;
    private final HttpClient client;
    private final static String AUTH_HEADER_NAME = "authorization";

    public ServerFacade(int hostname, int port) {
        this.BASE_URL = String.format("http://%s:%d", hostname, port);
        client = HttpClient.newHttpClient();
    }

    public void login(String username, String password) {
        var body = Map.ofEntries(Map.entry("username", username), Map.entry("password", password));
        var jsonBody = new Gson().toJson(body);
        var response = post("/session", jsonBody);
    }

    public void logout() {
        var response = delete("/session");
    }

    public void register(String username, String password, String email) {
        var body = Map.ofEntries(Map.entry("username", username), Map.entry("password", password),
                Map.entry("email", email));
        var jsonBody = new Gson().toJson(body);
        var response = post("/user", jsonBody);
    }

    public void createGame(String gameName) {
        var body = Map.ofEntries(Map.entry("gameName", gameName));
        var jsonBody = new Gson().toJson(body);
        var response = post("/game", jsonBody);
    }

    public void listGames() {
        var response = get("/game");
    }

    public void playGame(int gameID, String playerColor) {
        var body = Map.ofEntries(Map.entry("gameID", gameID), Map.entry("playerColor", playerColor));
        var jsonBody = new Gson().toJson(body);
        var response = put("/game", jsonBody);
    }

    public void observeGame(int gameID) {

    }

    private URI getUri(String path) {
        try {
            return new URI(BASE_URL + "/session");
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
        HttpRequest request = HttpRequest.newBuilder(getUri(path)).DELETE().build();
        return client.send(request, BodyHandlers.ofString());
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
