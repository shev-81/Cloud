package messages;
public class FileMessage extends AbstractMessage {
    public String filename;
    public int partNumber;
    public int partsCount;
    public byte[] data;

    public FileMessage(String filename, int partNumber, int partsCount, byte[] data) {
        this.filename = filename;
        this.partNumber = partNumber;
        this.partsCount = partsCount;
        this.data = data;
    }
}

