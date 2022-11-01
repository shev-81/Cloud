package messages;

import lombok.NoArgsConstructor;

/**
 * The successor class {@link AbstractMessage AbstractMessage}, is a
 * Data Transfer Object, designed to transfer bytes of a file.
 */
@NoArgsConstructor
public class FileMessage extends AbstractMessage {

    /**
     * The file name.
     */
    public String filename;

    /**
     * The number of the current part of the file. (Parts of the
     * size of the transmitted information do not exceed 10 mb).
     */
    public int partNumber;

    /**
     * Number is an indicator of how many parts a file is cut
     * into in total.
     */
    public int partsCount;

    /**
     * Bytes of the file. Array no more than 10 mb.
     */
    public byte[] data;

    /**
     * When creating a message containing file data, the following
     * is determined: file name, the assigned parcel number, how
     * many parcels there are in total, and the byte array itself
     * containing part of the file.
     * @param filename The file name.
     * @param partNumber The assigned parcel number.
     * @param partsCount How many packages are there in total.
     * @param data An array of bytes containing a part of the file.
     */
    public FileMessage(String filename, int partNumber, int partsCount, byte[] data) {
        this.filename = filename;
        this.partNumber = partNumber;
        this.partsCount = partsCount;
        this.data = data;
    }
}

