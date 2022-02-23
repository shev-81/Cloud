package messages;

public class DellFileRequest extends AbstractMessage {

    private String nameFile;

    public DellFileRequest(String nameFile) {
        this.nameFile = nameFile;
    }

    public String getNameFile() {
        return nameFile;
    }
}
