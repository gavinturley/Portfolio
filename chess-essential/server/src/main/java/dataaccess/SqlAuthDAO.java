package dataaccess;

import model.AuthData;
import model.UserData;

import java.sql.SQLException;

public class SqlAuthDAO implements AuthDAO{
    public SqlAuthDAO() throws DataAccessException {
        configureDatabase();
    }

    public void createAuth(AuthData auth) throws DataAccessException {
        String statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection()){
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setString(1, auth.authToken());
            preparedStatement.setString(2, auth.username());
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        String statement = "SELECT authToken, username FROM auth WHERE authToken=?";
        try (var conn = DatabaseManager.getConnection()){
            try (var preparedStatement = conn.prepareStatement(statement)){
                preparedStatement.setString(1, authToken);
                try (var resultSet = preparedStatement.executeQuery()){
                    if (resultSet.next()) {
                        return new AuthData(resultSet.getString("authToken"), resultSet.getString("username"));
                    }
                }
            }
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
        return null;
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        String statement = "DELETE FROM auth WHERE authToken=?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
    }

    public void clear() throws DataAccessException {
        var statement = "DELETE FROM auth";
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
    }

    public void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();

        String statement = """
                    CREATE TABLE IF NOT EXISTS auth (
                    authToken VARCHAR(255) NOT NULL PRIMARY KEY,
                    username VARCHAR(255) NOT NULL
                    );
                    """;
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage());
        }
    }
}
