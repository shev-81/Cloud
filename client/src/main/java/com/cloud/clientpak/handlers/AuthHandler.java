package com.cloud.clientpak.handlers;

import com.cloud.clientpak.Controller;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import messages.AuthMessage;
import messages.FileInfo;

/**
 * Класс слушатель сообщений авторизации. Определяет методы по работе
 * с сообщением авторизации.
 */
public class AuthHandler{

    /**
     * Переменная {@link Controller Controller}
     */
    private Controller controller;

    /**
     * Конструктор сохраняет ссылку на контроллер приложения.
     * @param controller контроллер приложения.
     */
    public AuthHandler(Controller controller) {
        this.controller = controller;
    }

    /**
     * Если вызывается этот метод, это означает, что сервер обработал запрос авторизации и в
     * {@link AuthMessage AuthMessage}  вернул результат.
     * @param ctx channel context.
     * @param msg объект сообщение.
     */
    public void authHandle(ChannelHandlerContext ctx, Object msg) {
        AuthMessage authMsg = (AuthMessage) msg;
        if(!authMsg.getLoginUser().equals("none")){
            openCloudWindow(authMsg);
        }else{
            authNo();
        }
    }

    /**
     * Открывает панель GUI Облака, подгружает список объектов описывающих файлы на сервере
     * {@link FileInfo FileInfo}.
     * @param authMsg объект сообщение.
     */
    public void openCloudWindow(AuthMessage authMsg){
        controller.setFileList(authMsg.getListFiles());
        controller.reloadFxFilesList(authMsg.getListFiles());
        controller.changeStageToCloud();
    }

    /**
     * Вызывается при отказе сервером в авторизации.
     */
    public void authNo(){
        Platform.runLater(() ->{
            controller.getAuthMessage().setVisible(true);
            controller.getAuthMessage().setText("Авторизация не пройдена!");
        });
    }
}
