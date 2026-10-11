package handler;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.RegisterRequest;
import service.RegisterResult;
import service.UserService;

import java.util.Map;

/**
 * The server handler classes serve as a translator between HTTP and Java.
 * Your handlers will convert an HTTP request into Java usable objects & data.
 * The handler then calls the appropriate service.
 * When the service responds, the handler converts the response object back to JSON and sends the HTTP response.
 * This could include converting thrown exception types into the appropriate HTTP status codes if necessary.
 */
public class UserHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void registerUser(Context ctx) {
        try {
            RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
            RegisterResult result = userService.register(request);
            sendJsonResponse(ctx, 200, result);

        } catch (BadRequestException e) {
            sendJsonResponse(ctx, 400, Map.of("message", e.getMessage()));
        } catch (AlreadyTakenException e) {
            sendJsonResponse(ctx, 403, Map.of("message", e.getMessage()));
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
