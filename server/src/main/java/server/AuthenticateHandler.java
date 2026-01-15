package server;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import dataaccess.AuthDAO;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import model.AuthData;
import service.UnauthorizedException;

public class AuthenticateHandler implements Handler {
    private AuthDAO authDAO;

    public AuthenticateHandler(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String authToken = ctx.headerAsClass("authorization", String.class).get();
        try {
            UUID.fromString(authToken);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException(null);
        }
        AuthData authSession = authDAO.getAuth(authToken);
        if (authSession == null) {
            throw new UnauthorizedException(null);
        }
    }

}
