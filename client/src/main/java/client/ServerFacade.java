package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import service.*;

import java.io.*;
import java.net.*;
import java.util.Collection;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    // --- User & Auth Endpoints ---

    public void clearDatabase() throws Exception {
        var path = "/db";
        makeRequest("DELETE", path, null, null, null);
    }

    public AuthData register(String username, String password, String email) throws Exception {
        var path = "/user";
        var requestBody = new RegisterRequest(username, password, email);
        return makeRequest("POST", path, requestBody, null, AuthData.class);
    }

    public AuthData login(String username, String password) throws Exception {
        var path = "/session";
        var requestBody = new LoginRequest(username, password);
        return makeRequest("POST", path, requestBody, null, AuthData.class);
    }

    public void logout(String authToken) throws Exception {
        var path = "/session";
        makeRequest("DELETE", path, null, authToken, null);
    }

    // --- Game Management Endpoints ---

    public Collection<GameData> listGames(String authToken) throws Exception {
        var path = "/game";
        var result = makeRequest("GET", path, null, authToken, ListGamesResult.class);
        return result != null ? result.games() : java.util.Collections.emptyList();
    }

    public GameData createGame(String authToken, String gameName) throws Exception {
        var path = "/game";
        var requestBody = new CreateGameRequest(authToken, gameName);
        var result = makeRequest("POST", path, requestBody, authToken, CreateGameResult.class);
        // White username and black username both start out null
        return new GameData(result.gameID(), null, null, gameName, new chess.ChessGame());
    }

    // --- Helper methods ---

    private final Gson gson = new Gson();

    private <T> T makeRequest(String method, String path, Object request, String authToken, Class<T> responseClass) throws Exception {
        try {
            // Open the connection
            URL url = new URI(serverUrl + path).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);

            // Add Authorization header if provided
            if (authToken != null && !authToken.isEmpty()) {
                http.addRequestProperty("authorization", authToken);
            }

            // Add request body if present (POST, PUT)
            if (request != null) {
                http.setDoOutput(true);
                http.addRequestProperty("Content-Type", "application/json");
                String json = gson.toJson(request);
                try (OutputStream reqBody = http.getOutputStream()) {
                    reqBody.write(json.getBytes());
                }
            }

            http.connect();

            // Check for errors
            throwIfFailed(http);

            // Read and deserialize response body
            return readBody(http, responseClass);

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private void throwIfFailed(HttpURLConnection http) throws IOException {
        var status = http.getResponseCode();
        if (status / 100 != 2) {
            try (InputStream responseBody = http.getErrorStream()) {
                if (responseBody != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody))) {
                        throw new IOException(reader.readLine());
                    }
                } else {
                    throw new IOException("Server failed with status code: " + status);
                }
            }
        }
    }

    private <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (responseClass != null) {
            try (InputStream respBody = http.getInputStream()) {
                if (respBody != null) {
                    InputStreamReader reader = new InputStreamReader(respBody);
                    response = gson.fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }
}