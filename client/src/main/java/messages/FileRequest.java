package messages;

import lombok.Data;

/**
 * Класс наследник {@link AbstractMessage AbstractMessage}, является Data Transfer Object,
 * является служебной командой - запросом файла из облака.
 */
@Data
public class FileRequest extends AbstractMessage {

    /**
     * Имя файла.
     */
    private String filename;

    /**
     * Конструктор определяет имя файла
     * @param filename Имя файла.
     */
    public FileRequest(String filename) {
        this.filename = filename;
    }
}
