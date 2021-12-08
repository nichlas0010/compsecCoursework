package rocks.ashleigh.lovejoy.jpa;

import rocks.ashleigh.lovejoy.datastructures.EvaluationRequest;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.File;

@Entity
public class EvaluationEntity {

    public EvaluationEntity() {}

    public EvaluationEntity(String name, EvaluationRequest request) {
        this.name = name;
        comments = request.getComments();
        contactType = request.getContactType();
        image = new File(request.getImage().getPath());
        System.out.println("paths");
        System.out.println(request.getImage().getPath());
        System.out.println(request.getImage().getAbsolutePath());
        try {
            System.out.println(request.getImage().getCanonicalPath());
        } catch (Exception e) {}

    }

    @Id
    @GeneratedValue
    private int id;
    private String name;
    private String comments;
    private String contactType;
    private File image;
}
