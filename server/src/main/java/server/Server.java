package server;

import com.google.gson.Gson;
import handler.Handler;
import io.javalin.*;
import io.javalin.http.Context;

import java.util.Map;

/**
 * The Server receives network HTTP requests and sends them to the correct handler for processing.
 * The server should also handle all unhandled exceptions that your application generates
 * and return the appropriate HTTP status code.
 */
public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.delete("/db", Handler::clearDb);

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
