package messages;

import lombok.Data;

/**
 * Класс наследник {@link AbstractMessage AbstractMessage}, является Data Transfer Object,
 * несет в себе название файла, который нужно удалить с облака.
 */
@Data
public class DelFileRequest extends AbstractMessage {

    /**
     * Имя файла.
     */
    private String nameFile;

    /**
     * Конструктор с переменной имя файла.
     * @param nameFile имя файла.
     */
    public DelFileRequest(String nameFile) {
        this.nameFile = nameFile;
    }
}
