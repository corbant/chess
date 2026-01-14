package dataaccess;

import java.util.ArrayList;
import java.util.Collection;

import model.UserData;

public class MemoryUserDAO implements UserDAO {
    private Collection<UserData> users;

    public MemoryUserDAO() {
        users = new ArrayList<>();
    }

    @Override
    public UserData createUser(String username, String password, String email) {
        UserData user = new UserData(username, password, email);
        users.add(user);
        return user;
    }

    @Override
    public UserData getUser(String username) {
        for (UserData user : users) {
            if (user.username() == username) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void clear() {
        users = new ArrayList<>();
    }

}
