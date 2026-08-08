package service;

import dataaccess.*;

public class ClearService {
    private final GameDAO gameDAO;
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    /* Implements Service logic dealing with the clear() endpoint */

    public ClearService(GameDAO gameDAO, UserDAO userDAO, AuthDAO authDAO){
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public void clear() throws DataAccessException {
        authDAO.clear();
        gameDAO.clear();
        userDAO.clear();
    }
}
