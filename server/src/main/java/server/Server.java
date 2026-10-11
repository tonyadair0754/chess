package server;

import dataaccess.*;
import handler.DatabaseHandler;
import handler.UserHandler;
import io.javalin.*;
import service.DatabaseService;
import service.UserService;

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
        UserService userService = new UserService(userDao, authDao);

        DatabaseHandler databaseHandler = new DatabaseHandler(databaseService);
        UserHandler userHandler = new UserHandler(userService);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // --- Endpoints ---
        javalin.delete("/db", databaseHandler::clearDb);
        javalin.post("/user", userHandler::registerUser);
        javalin.post("/session", userHandler::loginUser);
        javalin.delete("/session", userHandler::logoutUser);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
