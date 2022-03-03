package messages;

import lombok.Data;

import java.util.List;

@Data
public class FilesSizeRequest extends AbstractMessage{

    private long filesSize;
    private List<FileInfo> listFiles;
    private int partNumber;
    private int partsCount;

    public FilesSizeRequest(long filesSize) {
        this.filesSize = filesSize;
    }

    public FilesSizeRequest(long filesSize, List<FileInfo> listFiles) {
        this.filesSize = filesSize;
        this.listFiles = listFiles;
    }

    public FilesSizeRequest(long filesSize, List<FileInfo> listFiles, int partNumber, int partsCount) {
        this.filesSize = filesSize;
        this.listFiles = listFiles;
        this.partNumber = partNumber;
        this.partsCount = partsCount;
    }

    public List<FileInfo> getListFiles() {
        return listFiles;
    }
}
