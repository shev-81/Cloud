package messages;

import lombok.Data;

import java.util.List;

@Data
public class AuthMessage extends AbstractMessage{

    private String loginUser;
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
