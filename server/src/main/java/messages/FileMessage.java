package messages;

/**
 * Класс наследник {@link AbstractMessage AbstractMessage}, является Data Transfer Object,
 * предназначен для передачи байтов файла.
 */
public class FileMessage extends AbstractMessage {

    /**
     * Имя файла.
     */
    public String filename;

    /**
     * Номер текущей части файла. (Части по размерам передаваемой информации не превышают 10 mb).
     */
    public int partNumber;

    /**
     * Число показатель, на сколько всего частей разрезан файл.
     */
    public int partsCount;

    /**
     * Байты файла. Массив не более 10 mb/
     */
    public byte[] data;

    /**
     * При создании сообщения несущего в себе данные файла определяеются: имя файла,
     * присвоенный номер посылки, сколько всего посылок и сам массив байт содержащий часть файла.
     * @param filename Имя файла.
     * @param partNumber Присвоенный номер посылки.
     * @param partsCount Сколько всего посылок.
     * @param data Массив байт содержащий часть файла.
     */
    public FileMessage(String filename, int partNumber, int partsCount, byte[] data) {
        this.filename = filename;
        this.partNumber = partNumber;
        this.partsCount = partsCount;
        this.data = data;
    }
}

