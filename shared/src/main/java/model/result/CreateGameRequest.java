package model.result;

public record CreateGameRequest(
        String authToken,
        String gameName
) {
}
