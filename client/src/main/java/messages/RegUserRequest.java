package messages;

import lombok.Data;

/**
 * Класс наследник {@link AbstractMessage AbstractMessage}, является Data Transfer Object.
 * Является служебным сообщением содержащим данные о регистрации нового пользователя.
 */
@Data
public class RegUserRequest extends AbstractMessage {

    /**
     * Имя пользователя.
     */
    private String nameUser;

    /**
     * Логин пользователя.
     */
    private String login;

    /**
     * Пароль пользователя.
     */
    private String passUser;

    /**
     * Конструктор наполняет все поля.
     * @param nameUser Имя пользователя.
     * @param login Логин пользователя.
     * @param passUser Пароль пользователя.
     */
    public RegUserRequest(String nameUser, String login, String passUser) {
        this.nameUser = nameUser;
        this.login = login;
        this.passUser = passUser;
    }
}

