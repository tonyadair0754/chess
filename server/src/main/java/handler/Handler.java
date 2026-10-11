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
public class Handler {
    private final DatabaseService databaseService;
    private final Gson gson = new Gson();

    public Handler(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public void clearDb(Context ctx) {
        try {
            databaseService.clear();
            ctx.status(200);
            ctx.result(gson.toJson(Map.of()));
            ctx.contentType("application/json");
        } catch (DataAccessException e) {
            ctx.status(500);
            ctx.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
            ctx.contentType("application/json");
        }
    }
}
