package messages;

public class FilesSizeMessage extends AbstractMessage{

    private long filesSize;

    public FilesSizeMessage(long filesSize) {
        this.filesSize = filesSize;
    }

    public long getFilesSize() {
        return filesSize;
    }
}
