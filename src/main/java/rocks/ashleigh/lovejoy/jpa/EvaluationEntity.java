package rocks.ashleigh.lovejoy.jpa;

import rocks.ashleigh.lovejoy.datastructures.EvaluationRequest;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.File;
import java.nio.file.Files;

@Entity
public class EvaluationEntity {

    public EvaluationEntity() {}

    public EvaluationEntity(String name, EvaluationRequest request) {
        this.name = name;
        comments = request.getComments();
        contactType = request.getContactType();
        try {
            image = Files.readAllBytes(request.getImage().toPath());
        } catch (Exception e) {System.out.println("shit's fucked");}

    }

    @Id
    @GeneratedValue
    private int id;
    private String name;
    private String comments;
    private String contactType;
    private byte[] image;
}
