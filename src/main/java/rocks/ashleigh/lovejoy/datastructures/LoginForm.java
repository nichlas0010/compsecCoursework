package rocks.ashleigh.lovejoy.datastructures;

import java.util.ArrayList;

public class LoginForm {
    private String username;
    private String password;
    private boolean isAdmin = false;

    public boolean computeValidity() {

        // TODO: Code to check if username and password are correct
        return true;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
