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
    // public LoginResult login(LoginRequest loginRequest) {}
    // public void logout(LogoutRequest logoutRequest) {}
}