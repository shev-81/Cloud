package messages;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The successor class {@link AbstractMessage AbstractMessage}, is a
 * Data Transfer Object, carries the name of the file to be deleted
 * from the cloud.
 */
@Data
@NoArgsConstructor
public class DelFileRequest extends AbstractMessage {

    /**
     * The file name.
     */
    private String nameFile;

    /**
     * Constructor with variable file name.
     * @param nameFile the file name.
     */
    public DelFileRequest(String nameFile) {
        this.nameFile = nameFile;
    }
}
