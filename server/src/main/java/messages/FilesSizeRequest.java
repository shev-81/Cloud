package messages;

import lombok.Data;

import java.util.List;

/**
 * The descendant class of AbstractMessage,
 * is a Data Transfer Object. If sent by the client, then the
 * constructor with the parameter "1" is used, accepting this
 * the message object server will understand that it is necessary
 * to return a completed response. On the server side, it is used
 * as a response to the client's request for storage capacity, and
 * the response to receiving parts of the files.
 */
@Data
public class FilesSizeRequest extends AbstractMessage{

    /**
     * Total file size in bytes.
     */
    private long filesSize;

    /**
     * A list of objects describing files on the server.
     */
    private List<FileInfo> listFiles;

    /**
     * Filled in in response to receiving part of the file.
     */
    private int partNumber;

    /**
     * Is filled in in response to receiving a part of the file,
     * how many parts are there in total.
     */
    private int partsCount;

    /**
     * A constructor filled in by the client for a request to the server.
     * @param filesSize contains "1".
     */
    public FilesSizeRequest(long filesSize) {
        this.filesSize = filesSize;
    }

    /**
     * The constructor used by the server.
     * @param filesSize The size of files on the server in bytes.
     * @param listFiles A list of objects describing files.
     */
    public FilesSizeRequest(long filesSize, List<FileInfo> listFiles) {
        this.filesSize = filesSize;
        this.listFiles = listFiles;
    }

    /**
     * The constructor used by the server.
     * @param filesSize The size of files on the server in bytes.
     * @param listFiles A list of objects describing files.
     * @param partNumber The assigned parcel number.
     * @param partsCount How many packages are there in total.
     */
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
