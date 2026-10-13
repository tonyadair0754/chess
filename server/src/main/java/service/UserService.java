package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserService {
    private final UserDAO userDao;
    private final AuthDAO authDao;

    public UserService(UserDAO userDao, AuthDAO authDao) {
        this.userDao = userDao;
        this.authDao = authDao;
    }

    public RegisterResult register(RegisterRequest registerRequest)
            throws DataAccessException, AlreadyTakenException, BadRequestException {

        // Validate inputs
        if (registerRequest == null ||
                registerRequest.username() == null || registerRequest.username().isBlank() ||
                registerRequest.password() == null || registerRequest.password().isBlank() ||
                registerRequest.email() == null || registerRequest.email().isBlank()) {
            throw new BadRequestException("Error: bad request");
        }

        // Check if username is already taken
        if (userDao.getUser(registerRequest.username()) != null) {
            throw new AlreadyTakenException("Error: already taken");
        }

        // Create and save new user
        UserData newUser = new UserData(
                registerRequest.username(),
                registerRequest.password(),
                registerRequest.email()
        );
        userDao.createUser(newUser);

        // Create and save new auth token
        String token = UUID.randomUUID().toString();
        AuthData newAuth = new AuthData(token, registerRequest.username());
        authDao.createAuth(newAuth);

        return new RegisterResult(registerRequest.username(), token);
    }

    public LoginResult login(LoginRequest loginRequest)
            throws DataAccessException, UnauthorizedException, BadRequestException {

        // Validate inputs
        if (loginRequest == null ||
                loginRequest.username() == null || loginRequest.username().isBlank() ||
                loginRequest.password() == null || loginRequest.password().isBlank()) {
            throw new BadRequestException("Error: bad request");
        }

        // Fetch stored user record from DAO
        UserData user = userDao.getUser(loginRequest.username());

        // Check if username already exists and if passwords match
        if (userDao.getUser(loginRequest.username()) == null
                || !user.password().equals(loginRequest.password())) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        // Create and save new auth token
        String token = UUID.randomUUID().toString();
        AuthData newAuth = new AuthData(token, loginRequest.username());
        authDao.createAuth(newAuth);

        return new LoginResult(loginRequest.username(), token);
    }

    public void logout(LogoutRequest logoutRequest)
            throws DataAccessException, UnauthorizedException {

        // Validate inputs
        if (logoutRequest == null ||
                logoutRequest.authToken() == null || logoutRequest.authToken().isBlank()) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        // Check if token exists in database/memory
        AuthData auth = authDao.getAuth(logoutRequest.authToken());
        if (auth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        // Delete session token
        authDao.deleteAuth(logoutRequest.authToken());
    }
}