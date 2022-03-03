package messages;

import lombok.Data;

@Data
public class RegUserRequest extends AbstractMessage {

    private String nameUser;
    private String login;
    private String passUser;

    public RegUserRequest(String nameUser, String login, String passUser) {
        this.nameUser = nameUser;
        this.login = login;
        this.passUser = passUser;
    }
}

