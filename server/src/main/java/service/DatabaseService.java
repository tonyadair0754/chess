package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

public class DatabaseService {
    private UserDAO UserDAO;
    private AuthDAO AuthDAO;
    private GameDAO GameDAO;

    public DatabaseService(UserDAO UserDAO, AuthDAO AuthDAO, GameDAO GameDAO) {
        this.UserDAO = UserDAO;
        this.AuthDAO = AuthDAO;
        this.GameDAO = GameDAO;
    }

    public void clear() {

    }
}
