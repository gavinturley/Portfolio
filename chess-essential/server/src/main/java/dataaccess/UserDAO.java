package dataaccess;

import model.UserData;

// Data Access Object for user data
public interface UserDAO {
    void createUser(UserData u) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void clear() throws DataAccessException;
}
