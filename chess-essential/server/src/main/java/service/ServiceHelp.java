package service;

import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import dataaccess.AuthDAO;
import model.AuthData;

public class ServiceHelp {

    public static AuthData requireAuth(AuthDAO authDAO, String authToken) throws DataAccessException {
        if (authToken == null){
            throw new UnauthorizedException("Error: unauthorized");
        }
        var auth = authDAO.getAuth(authToken);
        if (auth == null){
            throw new UnauthorizedException("Error: unauthorized");
        }
        return auth;
    }

    public static boolean isBlank(String string) {
        return string == null || string.isBlank();
    }
}
