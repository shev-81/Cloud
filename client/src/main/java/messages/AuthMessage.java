package messages;

import lombok.Data;

import java.util.List;

/**
 * The successor class {@link AbstractMessage AbstractMessage},
 * is a Data Transfer Object, carrying the user's login and
 * password to the server, and upon successful authorization
 * contains contains a list of files in the cloud.
 */
@Data
@Message
public class AuthMessage extends AbstractMessage{

    /**
     * User login.
     */
    private String loginUser;

    /**
     * The user's password.
     */
    private String passUser;

    /**
     * A list of objects describing files on the server.
     * @see FileInfo
     */
    private List<FileInfo> listFiles;

    /**
     * The constructor used on the client side to fill in the
     * login and password.
     * @param loginUser user login.
     * @param passUser the user's password.
     */
    public AuthMessage(String loginUser, String passUser) {
        this.loginUser = loginUser;
        this.passUser = passUser;
    }

    /**
     * A constructor used on the server side to respond with a populated
     * list of objects describing files in the cloud.
     * @param loginUser user login.
     * @param listFiles  list of files in the cloud.
     */
    public AuthMessage(String loginUser, List<FileInfo> listFiles) {
        this.loginUser = loginUser;
        this.listFiles = listFiles;
    }

    public List<FileInfo> getListFiles() {
        return listFiles;
    }
}
