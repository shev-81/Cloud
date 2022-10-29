package messages;

import lombok.Data;

/**
 * The descendant class of {@link AbstractMessage AbstractMessage}, is a Data Transfer Object.
 * Is a service message containing data about the registration of a new user.
 * */
@Data
@Message
public class RegUserRequest extends AbstractMessage {

    /**
     * Username.
     */
    private String nameUser;

    /**
     * User login.
     */
    private String login;

    /**
     * The user's password.
     */
    private String passUser;

    /**
     * The constructor fills in all fields.
     * @param nameUser Username.
     * @param login User login.
     * @param passUser The user's password.
     */
    public RegUserRequest(String nameUser, String login, String passUser) {
        this.nameUser = nameUser;
        this.login = login;
        this.passUser = passUser;
    }
}

