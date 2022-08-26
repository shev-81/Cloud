package messages;

import lombok.Data;

/**
 * The successor class {@link AbstractMessage AbstractMessage}, is
 * a Data Transfer Object, is a service command that requests a
 * file from the cloud.
 */
@Data
public class FileRequest extends AbstractMessage {

    /**
     * The file name.
     */
    private String filename;

    /**
     * The constructor defines the file name.
     * @param filename The file name.
     */
    public FileRequest(String filename) {
        this.filename = filename;
    }
}
