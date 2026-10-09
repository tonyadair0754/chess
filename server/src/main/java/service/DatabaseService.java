package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

public class DatabaseService {
    private UserDAO userDao;
    private AuthDAO authDao;
    private GameDAO gameDao;

    public DatabaseService(UserDAO userDao, AuthDAO authDao, GameDAO gameDao) {
        this.userDao = userDao;
        this.authDao = authDao;
        this.gameDao = gameDao;
    }

    public void clear() throws DataAccessException {
        userDao.clear();
        authDao.clear();
        gameDao.clear();
    }
}
