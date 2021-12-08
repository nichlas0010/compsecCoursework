package rocks.ashleigh.lovejoy.jpa;

import rocks.ashleigh.lovejoy.datastructures.LoginForm;
import rocks.ashleigh.lovejoy.datastructures.RegistrationForm;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UserEntity {

    public UserEntity() {}

    public UserEntity(RegistrationForm form) {
        name = form.getName();
        emailAddress = form.getEmailAddress();
        password = form.getPassword();
        phoneNumber = form.getPhoneNumber();

        // TODO: Encrypt the password
    }

    @Id
    private String name;
    private String emailAddress;
    private String password;
    private String phoneNumber;
}
