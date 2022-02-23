package messages;

public class RegUserRequest extends AbstractMessage {

    private String nameUser;
    private String login;
    private String passUser;

    public RegUserRequest(String nameUser, String login, String passUser) {
        this.nameUser = nameUser;
        this.login = login;
        this.passUser = passUser;

    }

    public String getNameUser() {
        return nameUser;
    }

    public String getLogin() {
        return login;
    }

    public String getPassUser() {
        return passUser;
    }
}

