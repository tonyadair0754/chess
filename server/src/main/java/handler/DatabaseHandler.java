package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.DatabaseService;

import java.util.Map;

/**
 * The server handler classes serve as a translator between HTTP and Java.
 * Your handlers will convert an HTTP request into Java usable objects & data.
 * The handler then calls the appropriate service.
 * When the service responds, the handler converts the response object back to JSON and sends the HTTP response.
 * This could include converting thrown exception types into the appropriate HTTP status codes if necessary.
 */
public class DatabaseHandler {
    private final DatabaseService databaseService;
    private final Gson gson = new Gson();

    public DatabaseHandler(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public void clearDb(Context ctx) {
        try {
            databaseService.clear();
            sendJsonResponse(ctx, 200, Map.of());
        } catch (DataAccessException e) {
            sendJsonResponse(ctx, 500, Map.of("message", e.getMessage()));
        }
    }

    private void sendJsonResponse(Context ctx, int status, Object body) {
        ctx.status(status);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(body));
    }
}
