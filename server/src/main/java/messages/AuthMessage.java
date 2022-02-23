package messages;

public class AuthMessage extends AbstractMessage{

    private String loginUser;
    private String passUser;

    public AuthMessage(String loginUser, String passUser) {
        this.loginUser = loginUser;
        this.passUser = passUser;
    }

    public String getLoginUser() {
        return loginUser;
    }

    public String getPassUser() {
        return passUser;
    }
}
