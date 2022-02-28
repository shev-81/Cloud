package messages;

import java.util.List;

public class FilesSizeRequest extends AbstractMessage{

    private long filesSize;
    private List<FileInfo> listFiles;

    public FilesSizeRequest(long filesSize) {
        this.filesSize = filesSize;
    }

    public FilesSizeRequest(long filesSize, List<FileInfo> listFiles) {
        this.filesSize = filesSize;
        this.listFiles = listFiles;
    }

    public long getFilesSize() {
        return filesSize;
    }

    public List<FileInfo> getListFiles() {
        return listFiles;
    }
}
