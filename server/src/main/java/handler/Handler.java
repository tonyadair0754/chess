package handler;

import com.google.gson.Gson;
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
    public static void clearDb(Context ctx) {
        DatabaseService.clear();

        var r = Map.of();
        var Gson = new Gson();
        ctx.status(200);
        ctx.json(Gson.toJson(r));
    }

}
