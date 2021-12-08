package rocks.ashleigh.lovejoy.datastructures;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class EvaluationRequest {

    private String comments;
    private String contactType;
    private MultipartFile image;

    public boolean computeValidity() {
        if (comments.length() != 0 && (contactType == "telephone" || contactType == "email")) {
            return true;
        }
        return false;
    }
}
