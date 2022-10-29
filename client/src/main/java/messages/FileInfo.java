package messages;

import lombok.Data;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * A model describing the properties of the file.
 */
@Data
@Message
public class FileInfo  implements Serializable {

    /**
     * Type - file or directory.
     */
    public enum FileType {
        FILE("F"), DIRECTORY("D");

        private String name;

        public String getName() {
            return name;
        }

        FileType(String name) {
            this.name = name;
        }
    }

    /**
     * The file name.
     */
    private String filename;

    /**
     * File type.
     */
    private FileType type;

    /**
     * File size in bytes.
     */
    private long size;

    /**
     * The time of the last change.
     */
    private LocalDateTime lastModified;

    /**
     * The constructor takes a file path object and,
     * processing it, fills the model with properties.
     * @param path The path to the file.
     */
    public FileInfo(Path path) {
        try {
            this.filename = path.getFileName().toString();
            this.size = Files.size(path);
            this.type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            if (this.type == FileType.DIRECTORY) {
                this.size = -1L;
            }
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(3));
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file info from path");
        }
    }
}