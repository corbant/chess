package service.request;

public record GameJoinRequest(String playerColor, int gameID, String authToken) {

}
