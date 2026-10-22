package service;
package dataaccess;

import dataaccess.DatabaseManager;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTests {

    @Test
    void connectionWorks() throws Exception {
        DatabaseManager.createDatabase();

        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT 1+1")) {
                try (var resultSet = preparedStatement.executeQuery()) {
                    assertTrue(resultSet.next()); // Confirms a row was returned
                    assertEquals(2, resultSet.getInt(1));
                }
            }
        }
    }
}