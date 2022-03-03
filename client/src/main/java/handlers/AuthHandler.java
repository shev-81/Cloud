package handlers;

import com.cloud.clientpak.Controller;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import messages.AuthMessage;
import java.io.IOException;

public class AuthHandler{

    private Controller controller;

    public AuthHandler(Controller controller) {
        this.controller = controller;
    }

    public void authHandle(ChannelHandlerContext ctx, Object msg) {
        AuthMessage authMsg = (AuthMessage) msg;
        if(!authMsg.getLoginUser().equals("none")){
            try {
                openCloudWindow(authMsg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            authNo();
        }
    }

    public void openCloudWindow(AuthMessage authMsg) throws IOException {
        controller.setFileList(authMsg.getListFiles());
        controller.reloadFxFilesList(authMsg.getListFiles());
        controller.changeStageToCloud();
    }

    public void authNo(){
        Platform.runLater(() ->{
            controller.getAuthMessage().setVisible(true);
            controller.getAuthMessage().setText("Авторизация не пройдена!");
        });
    }
}
