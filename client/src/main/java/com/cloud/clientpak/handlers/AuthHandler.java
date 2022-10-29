package com.cloud.clientpak.handlers;

import com.cloud.clientpak.Controller;
import com.cloud.clientpak.interfaces.RequestHandler;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import messages.AuthMessage;
import messages.FileInfo;

/**
 * Authorization message listener class. Defines methods for work
 * with an authorization message.
 */
@Handler(message = "AuthMessage")
public class AuthHandler implements RequestHandler {

    /**
     * Variable {@link Controller Controller}
     */
    private Controller controller;

    /**
     * The constructor saves a reference to the application controller.
     * @param controller application controller.
     */
    public AuthHandler(Controller controller) {
        this();
        this.controller = controller;
    }

    public AuthHandler() {
    }

    /**
     * If this method is called, it means that the server processed the
     * authorization request and returned the result in {@link AuthMessage AuthMessage}.
     * @param ctx channel context.
     * @param msg message object.
     */
    @Override
    public void handle(ChannelHandlerContext ctx, Object msg) {
        AuthMessage authMsg = (AuthMessage) msg;
        if(!authMsg.getLoginUser().equals("none")){
            openCloudWindow(authMsg);
        }else{
            authNo();
        }
    }

    /**
     * Opens the Cloud GUI panel, loads a list of objects describing
     * files on the server {@link FileInfo FileInfo}.
     * @param authMsg message object.
     */
    public void openCloudWindow(AuthMessage authMsg){
        controller.setFileList(authMsg.getListFiles());
        controller.reloadFxFilesList(authMsg.getListFiles());
        controller.changeStageToCloud();
    }

    /**
     * Called when the server refuses authorization.
     */
    public void authNo(){
        Platform.runLater(() ->{
            controller.getAuthMessage().setVisible(true);
            controller.getAuthMessage().setText("Авторизация не пройдена!");
        });
    }
}
