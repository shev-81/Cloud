package messages;

public class FileSizeMessage extends AbstractMessage{

    private long filesSize;

    public FileSizeMessage(long filesSize) {
        this.filesSize = filesSize;
    }

    public long getFilesSize() {
        return filesSize;
    }
}
