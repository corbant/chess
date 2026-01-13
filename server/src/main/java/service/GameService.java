package service;

import service.request.GameCreateRequest;
import service.request.GameJoinRequest;
import service.request.GameListRequest;
import service.result.GameCreateResponse;
import service.result.GameListResponse;

public class GameService {
    public void join(GameJoinRequest gameJoinRequest) {
    }

    public GameCreateResponse create(GameCreateRequest gameCreateRequest) {
        return new GameCreateResponse();
    }

    public GameListResponse list(GameListRequest gameListRequest) {
        return new GameListResponse(null);
    }
}
