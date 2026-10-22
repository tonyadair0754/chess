package dataaccess;

import com.google.gson.Gson;
import model.UserData;

import java.sql.SQLException;

public class UserDAOMySQL extends SqlDAO implements UserDAO {

    public UserDAOMySQL() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var createTableStatement = """
                        CREATE TABLE IF NOT EXISTS user (
                        username VARCHAR(255) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        email VARCHAR(255) NOT NULL,
                        PRIMARY KEY (username))
                    """;
            try (var preparedStatement = conn.prepareStatement(createTableStatement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to configure user database", e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        var statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        executeUpdate(statement, user.username(), user.password(), user.email());
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var statement = "TRUNCATE user";
        executeUpdate(statement);
    }

    @Override
    public void clear() throws DataAccessException {

    }
}