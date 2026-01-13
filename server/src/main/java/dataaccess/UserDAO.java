package dataaccess;

import model.UserData;

public interface UserDAO {
    UserData createUser();

    UserData getUser(String username);
}
