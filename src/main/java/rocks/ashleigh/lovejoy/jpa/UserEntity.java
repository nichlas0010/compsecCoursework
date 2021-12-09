package rocks.ashleigh.lovejoy.jpa;

import lombok.Data;
import org.apache.tomcat.util.codec.binary.Base64;
import rocks.ashleigh.lovejoy.datastructures.RegistrationForm;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigInteger;
import java.security.MessageDigest;

@Data
@Entity
public class UserEntity {

    public UserEntity() {}

    public UserEntity(RegistrationForm form, String token) {
        name = form.getName();
        emailAddress = form.getEmailAddress();
        password = form.getPassword();
        phoneNumber = form.getPhoneNumber();
        this.token = token;
        emailConfirmed = false;
        isAdmin = false;

        try {
            password = new String(Base64.encodeBase64(MessageDigest.getInstance("SHA-256").digest((password + token).getBytes())));
        } catch (Exception e) {
            // This will always work, SHA-256 is going nowhere. But just in case, I'll print the error.
            throw new RuntimeException(e);
        }
    }

    public boolean comparePassword(String pass) {
        try {
            pass = new String(Base64.encodeBase64(MessageDigest.getInstance("SHA-256").digest((pass + token).getBytes())));
            return password.equals(pass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private String name;
    @Id
    private String emailAddress;
    private String password;
    private String phoneNumber;
    private String token;
    private boolean emailConfirmed;
    private boolean isAdmin;
}
