package server;

import dataaccess.*;
import handler.Handler;
import io.javalin.*;
import service.DatabaseService;

/**
 * The Server receives network HTTP requests and sends them to the correct handler for processing.
 * The server should also handle all unhandled exceptions that your application generates
 * and return the appropriate HTTP status code.
 */
public class Server {

    private final Javalin javalin;

    public Server() {
        // --- Create new instances for all layers ---
        UserDAO userDao = new UserDAOMemory();
        AuthDAO authDao = new AuthDAOMemory();
        GameDAO gameDao = new GameDAOMemory();

        DatabaseService databaseService = new DatabaseService(userDao, authDao, gameDao);

        Handler handler = new Handler(databaseService);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // --- Endpoints ---
        javalin.delete("/db", handler::clearDb);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
