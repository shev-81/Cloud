package messages;

import lombok.Data;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Модель описывающая свойства файла.
 */
@Data
public class FileInfo  implements Serializable {

    /**
     * Тип - файл или директория
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
     * Имя файла.
     */
    private String filename;

    /**
     * Тип файла.
     */
    private FileType type;

    /**
     * Размер файла в байтах.
     */
    private long size;

    /**
     * Вермя последнего изменения.
     */
    private LocalDateTime lastModified;

    /**
     * Конструктор принимает объект пути к файлу и обрабатывая его заполняет модель свойствами.
     * @param path Путь к файлу.
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