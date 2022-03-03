package messages;

import lombok.Data;

@Data
public class DelFileRequest extends AbstractMessage {

    private String nameFile;

    public DelFileRequest(String nameFile) {
        this.nameFile = nameFile;
    }
}
