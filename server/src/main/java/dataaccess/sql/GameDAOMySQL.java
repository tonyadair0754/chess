package dataaccess.sql;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.GameData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class GameDAOMySQL extends SqlDAO implements GameDAO {

    public GameDAOMySQL() throws DataAccessException {
        configureDatabase();
    }

    @Override
    protected void configureDatabase() throws DataAccessException {
        String[] createStatements = {
                """
                CREATE TABLE IF NOT EXISTS game (
                `gameID` INT NOT NULL AUTO_INCREMENT,
                `whiteUsername` VARCHAR(255),
                `blackUsername` VARCHAR(255),
                `gameName` VARCHAR(255) NOT NULL,
                `game` TEXT NOT NULL,
                PRIMARY KEY (`gameID`))
                """
        };
        executeCreateTables(createStatements);
    }

    @Override
    public void createGame(GameData gameData) throws DataAccessException {
        var statement = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
        String serializedGame = new Gson().toJson(gameData.game());
        executeUpdate(statement, gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), serializedGame);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game WHERE gameID=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to read game: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        Collection<GameData> games = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game";
            try (PreparedStatement ps = conn.prepareStatement(statement);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    games.add(readGame(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to list games: " + e.getMessage(), e);
        }
        return games;
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {
        var statement = "UPDATE game SET whiteUsername=?, blackUsername=?, gameName=?, game=? WHERE gameID=?";
        String serializedGame = new Gson().toJson(gameData.game());
        executeUpdate(
                statement,
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                serializedGame,
                gameData.gameID());
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE game";
        executeUpdate(statement);
    }

    // Helper method to map a ResultSet row into a GameData record
    private GameData readGame(ResultSet rs) throws SQLException {
        int id = rs.getInt("gameID");
        String white = rs.getString("whiteUsername");
        String black = rs.getString("blackUsername");
        String name = rs.getString("gameName");
        String jsonGame = rs.getString("game");

        // Deserialize JSON back into a ChessGame object
        ChessGame chessGame = new Gson().fromJson(jsonGame, ChessGame.class);

        return new GameData(id, white, black, name, chessGame);
    }
}