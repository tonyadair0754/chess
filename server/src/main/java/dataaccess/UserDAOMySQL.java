package dataaccess;

import model.UserData;

import java.sql.DriverManager;
import java.sql.SQLException;

import static java.text.Collator.PRIMARY;
import static jdk.incubator.vector.VectorOperators.NOT;

public class UserDAOMySQL implements UserDAO {

    public UserDAOMySQL throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {

            /*
            CREATE TABLE IF NOT EXISTS user (
                username VARCHAR(256) NOT NULL,
                password VARCHAR(256) NOT NULL,
                email VARCHAR(256) NOT NULL,
                PRIMARY KEY (username)
             */

        } catch (SQLException e) {
            throw new DataAccessException("Unable to configure user database");
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {

    }
}