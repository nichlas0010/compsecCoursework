package rocks.ashleigh.lovejoy.datastructures;

import lombok.Data;

import java.util.ArrayList;

@Data
public class ResetRequest {
    private String address;
    private String password;
    private String passwordConfirmation;
    private ArrayList<String> errors = new ArrayList<>();

    public boolean computeValidity() {

        errors.clear();

        if (!password.equals(passwordConfirmation)) {
            errors.add("Passwords do not match!");
        }

        if (!password.matches("(?=(.*[a-z]))(?=(.*[A-Z]))(?=(.*[0-9]))(?=(.*[!@#$%^&*()\\-__+.])).{8,}")) {
            errors.add("Password not strong enough! Must be at least 8 characters long, with at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character [!@#$%^&*()\\-__+.]");
        }

        if (password.length() > 32) {
            errors.add("Password too long! Must not be longer than 32 characters");
        }

        return errors.size() == 0;
    }
}
