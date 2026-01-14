package server;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import dataaccess.AuthDAO;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import model.AuthData;

public class AuthenticateHandler implements Handler {
    private AuthDAO authDAO;

    public AuthenticateHandler(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String authToken = ctx.headerAsClass("authorization", String.class).check(token -> {
            try {
                UUID.fromString(token);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }, "Invalid authorization token").getOrThrow(errors -> {
            throw new UnauthorizedResponse();
        });
        AuthData authSession = authDAO.getAuth(authToken);
        if (authSession == null) {
            throw new UnauthorizedResponse();
        }
    }

}
