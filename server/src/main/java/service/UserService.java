package service;

import dataaccess.*;
import dataaccess.sql.AuthDAO;
import dataaccess.sql.UserDAO;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserService {
    private final UserDAO userDao;
    private final AuthDAO authDao;

    public UserService(UserDAO userDao, AuthDAO authDao) {
        this.userDao = userDao;
        this.authDao = authDao;
    }

    public RegisterResult register(RegisterRequest request)
            throws DataAccessException, AlreadyTakenException, BadRequestException {

        // Validate inputs
        if (request == null ||
                request.username() == null || request.username().isBlank() ||
                request.password() == null || request.password().isBlank() ||
                request.email() == null || request.email().isBlank()) {
            throw new BadRequestException("Error: bad request");
        }

        // Check if username is already taken
        if (userDao.getUser(request.username()) != null) {
            throw new AlreadyTakenException("Error: already taken");
        }

        // Hash the plain-text password using BCrypt
        String hashedPassword = BCrypt.hashpw(request.password(), BCrypt.gensalt());

        // Create and save new user
        UserData newUser = new UserData(
                request.username(),
                hashedPassword,
                request.email()
        );
        userDao.createUser(newUser);

        // Create and save new auth token
        String token = UUID.randomUUID().toString();
        AuthData newAuth = new AuthData(token, request.username());
        authDao.createAuth(newAuth);

        return new RegisterResult(request.username(), token);
    }

    public LoginResult login(LoginRequest request)
            throws DataAccessException, UnauthorizedException, BadRequestException {

        // Validate inputs
        if (request == null ||
                request.username() == null || request.username().isBlank() ||
                request.password() == null || request.password().isBlank()) {
            throw new BadRequestException("Error: bad request");
        }

        // Fetch stored user record from DAO
        UserData user = userDao.getUser(request.username());

        // Check if username already exists and if passwords match
        if (userDao.getUser(request.username()) == null
                || !BCrypt.checkpw(request.password(), user.password())) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        // Create and save new auth token
        String token = UUID.randomUUID().toString();
        AuthData newAuth = new AuthData(token, request.username());
        authDao.createAuth(newAuth);

        return new LoginResult(request.username(), token);
    }

    public void logout(LogoutRequest request)
            throws DataAccessException, UnauthorizedException {

        // Validate inputs
        if (request == null ||
                request.authToken() == null || request.authToken().isBlank()) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        // Check if token exists in database/memory
        AuthData auth = authDao.getAuth(request.authToken());
        if (auth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        // Delete session token
        authDao.deleteAuth(request.authToken());
    }
}