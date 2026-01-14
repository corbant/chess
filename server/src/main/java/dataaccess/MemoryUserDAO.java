package dataaccess;

import java.util.ArrayList;

import model.UserData;

public class MemoryUserDAO implements UserDAO {
    private ArrayList<UserData> users;

    public MemoryUserDAO() {
        users = new ArrayList<>();
    }

    @Override
    public void createUser(UserData user) {
        users.add(user);
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
        users.clear();
    }

}
