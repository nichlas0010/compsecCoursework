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

        if (password != passwordConfirmation) {
            errors.add("Passwords do not match!");
        }

        if (name != name.replaceAll("[^a-zA-Z0-9]", "")) {
            errors.add("Username contains non-alphanumeric characters! Please only use a-Z and 0-9!");
        }

        // TODO: Check if passwords conform to policy



        // I asked Dr. Khan whether we should just accept UK phone numbers, and I didn't get an answer
        // So I'll just accept any string as input.
        if (phoneNumber.length() == 0) {
            errors.add("Please enter a phone number!");
        }


        valid = errors.size() == 0;
        return isValid();
    }

}
