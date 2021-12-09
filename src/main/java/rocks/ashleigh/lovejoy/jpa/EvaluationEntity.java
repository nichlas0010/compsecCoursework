package rocks.ashleigh.lovejoy.jpa;

import lombok.Data;
import rocks.ashleigh.lovejoy.datastructures.EvaluationRequest;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class EvaluationEntity {

    public EvaluationEntity() {}

    public EvaluationEntity(UserEntity user, EvaluationRequest request) {
        emailAddress = user.getEmailAddress();
        comments = request.getComments();
        contactType = request.getContactType();
        try {
            image = request.getImage().getBytes();
            if (image.length == 0) {
                hasImage = false;
            } else {
                hasImage = true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Id
    @GeneratedValue
    private int id;
    private String emailAddress;
    private String comments;
    private String contactType;
    private byte[] image;
    private boolean hasImage;
}
