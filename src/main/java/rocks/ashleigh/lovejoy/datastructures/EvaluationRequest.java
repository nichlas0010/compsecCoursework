package rocks.ashleigh.lovejoy.datastructures;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;

@Data
public class EvaluationRequest {

    private String comments;
    private String contactType;
    private MultipartFile image;

    private ArrayList<String> errors = new ArrayList<>();

    public boolean computeValidity() {
        errors.clear();

        if (comments.length() == 0 ) {
            errors.add("Please fill in the comments section!");
        }
        if (contactType == null || (!contactType.equals("phone") && !contactType.equals("email"))) {
            errors.add("Please select a contact method!");
        }

        return errors.size() == 0;
    }
}
