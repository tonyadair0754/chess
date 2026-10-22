package service;

import dataaccess.DatabaseManager;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTests {

    @Test
    void connectionWorks() throws Exception {
        DatabaseManager.createDatabase();

        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT 1+1")) {
                try (var rs = preparedStatement.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(2, rs.getInt(1));
                }
            }
        }
    }
}