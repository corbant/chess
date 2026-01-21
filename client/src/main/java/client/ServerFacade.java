package client;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Map;

public class ServerFacade {

    private final int PORT;
    private final String BASE_URL;

    public ServerFacade(int port) {
        this.PORT = port;
        this.BASE_URL = "http://localhost:" + port;
    }

    public void login(String username, String password) {

    }

    public void logout() {

    }

    public void register() {

    }

    public void createGame() {

    }

    public void listGames() {

    }

    public void playGame() {

    }

    public void observeGame() {

    }
}
