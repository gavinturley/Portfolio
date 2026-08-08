package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO{
    private final Map<String, UserData> users = new HashMap<>();

    @Override
    public void createUser(UserData user){
        var hashedUser = new UserData(user.username(), BCrypt.hashpw(user.password(), BCrypt.gensalt()), user.email());
        users.put(user.username(), hashedUser);
    }

    @Override
    public UserData getUser(String username){
        return users.get(username);
    }

    @Override
    public void clear(){
        users.clear();
    }
}