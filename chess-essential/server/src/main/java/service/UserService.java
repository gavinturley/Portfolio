package service;

import dataaccess.*;
import model.UserData;
import model.AuthData;
import org.mindrot.jbcrypt.BCrypt;
import server.AuthResult;
import server.LoginRequest;
import server.RegisterRequest;
import service.ServiceHelp;

import java.util.UUID;

/* Implements Service logic dealing with the users */

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO){
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthResult register(RegisterRequest request) throws DataAccessException {
        if (ServiceHelp.isBlank(request.username()) || ServiceHelp.isBlank(request.password()) || ServiceHelp.isBlank(request.email())){
            throw new BadRequestException("Error: bad request");
        }
        if (userDAO.getUser(request.username()) != null){
            throw new AlreadyTakenException("Error: already taken");
        }

        var user = new UserData(request.username(), request.password(), request.email());
        userDAO.createUser(user);

        return createAuth(user.username());
    }

    public AuthResult login(LoginRequest request) throws DataAccessException {
        if (ServiceHelp.isBlank(request.username()) || ServiceHelp.isBlank(request.password())){
            throw new BadRequestException("Error: bad request");
        }

        var user = userDAO.getUser(request.username());
        if (user == null || !BCrypt.checkpw(request.password(), user.password())){
            throw new UnauthorizedException("Error: unauthorized");
        }

        return createAuth(user.username());
    }

    public void logout(String authToken) throws DataAccessException {
        var auth = ServiceHelp.requireAuth(authDAO, authToken);
        authDAO.deleteAuth(auth.authToken());
    }

    private AuthResult createAuth(String username) throws DataAccessException {
        String authToken = generateToken();
        var auth = new AuthData(authToken, username);
        authDAO.createAuth(auth);
        return new AuthResult(auth.username(), auth.authToken());
    }

    private static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
