package handlers;

import com.cloud.clientpak.Controller;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import messages.RegUserRequest;

public class RegUserHandler{

    private Controller controller;

    public RegUserHandler(Controller controller) {
        this.controller =controller;
    }

    public void regHandle(ChannelHandlerContext ctx, Object msg) {
        RegUserRequest regUserRequest = (RegUserRequest) msg;
        if (regUserRequest.getNameUser().equals("none")) {
            Platform.runLater(() -> {
                controller.getRegMessage().setText("Регистрация не пройдена!");
                controller.getRegMessage().setVisible(true);
            });
        } else {
            Platform.runLater(() -> {
                controller.changeStageToAuth();
                controller.getAuthMessage().setText("Регистрация пройдена!");
                controller.getAuthMessage().setVisible(true);
            });
        }
    }
}
