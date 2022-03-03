package messages;

import lombok.Data;

@Data
public class FileRequest extends AbstractMessage {
    private String filename;

    public FileRequest(String filename) {
        this.filename = filename;
    }
}
