package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import service.RegisterRequest;

import java.io.*;
import java.net.*;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    // --- User & Auth Endpoints ---

    public AuthData register(String username, String password, String email) {
        var path = "/user";
        var requestBody = new RegisterRequest(username, password, email);
        return makeRequest("POST", path, requestBody, null, AuthData.class);
    }

    // --- Game Management Endpoints ---

    // --- Helper methods ---

    private <T> T makeRequest(String method, String path, Object request, String authToken, Class<T> responseClass) {
        // Build an HTTP request to serverUrl + path.
        // Serialize the request body into JSON.
        // Send the request and receive the response.
        // Check the HTTP status and deserialize the response into the requested type.
    }
}