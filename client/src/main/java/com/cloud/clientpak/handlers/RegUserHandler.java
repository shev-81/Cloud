package com.cloud.clientpak.handlers;

import com.cloud.clientpak.Controller;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import messages.RegUserRequest;

/**
 * Класс слушатель сообщений {@link RegUserRequest RegUserRequest}.
 */
public class RegUserHandler{

    /**
     * Переменная {@link Controller Controller}
     */
    private Controller controller;

    /**
     * Конструктор сохраняет ссылку на контроллер приложения.
     * @param controller контроллер приложения.
     */
    public RegUserHandler(Controller controller) {
        this.controller =controller;
    }

    /**
     * Обрабатывает служебное сообщение сервера о прохождении
     * или не прохождении регистрации.
     * @param ctx channel context.
     * @param msg объект сообщение.
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
