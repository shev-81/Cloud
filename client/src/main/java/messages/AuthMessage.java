package messages;

import lombok.Data;

import java.util.List;

/**
 * Класс наследник {@link AbstractMessage AbstractMessage}, является Data Transfer Object,
 * несущем в сторону сервера логин и пароль пользователя, а при успешной авторизации содержит
 * в себе список файлов в облаке.
 */
@Data
public class AuthMessage extends AbstractMessage{

    /**
     * Логин пользователя.
     */
    private String loginUser;

    /**
     * Пароль пользователя.
     */
    private String passUser;

    /**
     * Список объектов описывающих файлы на сервере.
     * @see FileInfo
     */
    private List<FileInfo> listFiles;

    /**
     * Конструктор используемый на стороне клиента для заполнения логина и пароля.
     * @param loginUser логин пользователя.
     * @param passUser пароль пользователя.
     */
    public AuthMessage(String loginUser, String passUser) {
        this.loginUser = loginUser;
        this.passUser = passUser;
    }

    /**
     * Конструктор используемый на стороне сервера для ответа с заполненым
     * списком объектов описывющих фалы в облаке.
     * @param loginUser логин пользователя.
     * @param listFiles  список файлов в облаке.
     */
    public AuthMessage(String loginUser, List<FileInfo> listFiles) {
        this.loginUser = loginUser;
        this.listFiles = listFiles;
    }

    public List<FileInfo> getListFiles() {
        return listFiles;
    }
}
