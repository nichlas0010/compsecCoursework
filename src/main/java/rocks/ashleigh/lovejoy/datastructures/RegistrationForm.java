package rocks.ashleigh.lovejoy.datastructures;

import java.util.ArrayList;

import lombok.Data;
import org.apache.commons.validator.routines.EmailValidator;
import rocks.ashleigh.lovejoy.jpa.UserRepository;

@Data
public class RegistrationForm {

    private String name;
    private String emailAddress;
    private String password;
    private String passwordConfirmation;
    private String phoneNumber;

    private boolean valid = false;
    private ArrayList<String> errors = new ArrayList<>();



    public boolean isValid() {
        return valid;
    }

    public ArrayList<String> getErrors() {
        return errors;
    }

    public boolean computeValidity(UserRepository userRepo) {

        errors.clear();

        // Check the email first
        if (!EmailValidator.getInstance().isValid(emailAddress)) {
            errors.add("Email address is invalid!");
        }
        if (userRepo.findByEmailAddress(emailAddress) != null) {
            errors.add("Email address is already in use!");
        }

        if (!password.equals(passwordConfirmation)) {
            errors.add("Passwords do not match!");
        }

        if (name != name.replaceAll("[^a-zA-Z0-9]", "")) {
            errors.add("Username contains non-alphanumeric characters! Please only use a-Z and 0-9!");
        }

        if (!password.matches("(?=(.*[a-z]))(?=(.*[A-Z]))(?=(.*[0-9]))(?=(.*[!@#$%^&*()\\-__+.])).{8,}")) {
            errors.add("Password not strong enough! Must be at least 8 characters long, with at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character [!@#$%^&*()\\-__+.]");
        }

        if (password.length() > 32) {
            errors.add("Password too long! Must not be longer than 32 characters");
        }

        // I asked Dr. Khan whether we should just accept UK phone numbers, and I didn't get an answer
        // So I'll just accept any string as input.
        if (phoneNumber.length() == 0) {
            errors.add("Please enter a phone number!");
        }


        valid = errors.size() == 0;
        return isValid();
    }

}
