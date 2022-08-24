package messages;

import lombok.Data;

import java.util.List;

/**
 * Класс наследник от {@link AbstractMessage AbstractMessage}.
 * Объект созданный на его основе является служебной командой авторизации.
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


    private List<FileInfo> listFiles;

    public AuthMessage(String loginUser, String passUser) {
        this.loginUser = loginUser;
        this.passUser = passUser;
    }

    public AuthMessage(String loginUser, List<FileInfo> listFiles) {
        this.loginUser = loginUser;
        this.listFiles = listFiles;
    }

    public List<FileInfo> getListFiles() {
        return listFiles;
    }
}
