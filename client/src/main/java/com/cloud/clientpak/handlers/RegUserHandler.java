package com.cloud.clientpak.handlers;

import com.cloud.clientpak.Controller;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import messages.RegUserRequest;

/**
 * Message Listener class {@link RegUserRequest RegUserRequest}.
 */
public class RegUserHandler{

    /**
     * Variable {@link Controller Controller}
     */
    private final Controller controller;

    /**
     * The constructor saves a reference to the application controller.
     * @param controller application controller.
     */
    public RegUserHandler(Controller controller) {
        this.controller =controller;
    }

    /**
     * Processes the server's service message about the passage
     * or not passing registration.
     * @param ctx channel context.
     * @param msg the message object.
     */
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
