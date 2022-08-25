package messages;

import lombok.Data;

import java.util.List;

/**
 * Класс наследник {@link AbstractMessage AbstractMessage}, является Data Transfer Object.
 * Если отправляется клиентом, то используется конструктор с параметром "1", приняв этот
 * объект сообщения сервер поймет, что необходимо вернуть заполненный ответ.
 * На стороне сервера используется как ответ клиенту на запрос о наполеннности хранилища, так
 * и ответ на получение частей файлов.
 */
@Data
public class FilesSizeRequest extends AbstractMessage{

    /**
     * Общий размер файлов в байтах.
     */
    private long filesSize;

    /**
     * Список объектов описывающих файлы на сервере.
     */
    private List<FileInfo> listFiles;

    /**
     * Заполянется в ответ о получении части файла.
     */
    private int partNumber;

    /**
     * Заполянется в ответ о получении части файла, сколько всего частей.
     */
    private int partsCount;

    /**
     * Конструктор заполняемый клиентом для запроса серверу.
     * @param filesSize содержит "1".
     */
    public FilesSizeRequest(long filesSize) {
        this.filesSize = filesSize;
    }

    /**
     * Конструктор используемый сервером.
     * @param filesSize Размер файлов на сервере в байтах.
     * @param listFiles Список объектов описывающих файлы.
     */
    public FilesSizeRequest(long filesSize, List<FileInfo> listFiles) {
        this.filesSize = filesSize;
        this.listFiles = listFiles;
    }

    /**
     * Конструктор используемый сервером.
     * @param filesSize Размер файлов на сервере в байтах.
     * @param listFiles Список объектов описывающих файлы.
     * @param partNumber Присвоенный номер посылки.
     * @param partsCount Сколько всего посылок.
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
