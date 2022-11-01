package com.cloud.clientpak.handlers;

import com.cloud.clientpak.Controller;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import messages.AuthMessage;
import messages.FileInfo;

/**
 * Authorization message listener class. Defines methods for work
 * with an authorization message.
 */
@Handler
public class AuthHandler extends AbstractHandler <AuthMessage> {

    /**
     * Variable {@link Controller Controller}
     */
    private Controller controller;

    /**
     * The constructor saves a reference to the application controller.
     * @param controller application controller.
     */
    public AuthHandler(Controller controller) {
        this.controller = controller;
    }

    /**
     * If this method is called, it means that the server processed the
     * authorization request and returned the result in {@link AuthMessage AuthMessage}.
     * @param ctx channel context.
     * @param msg message object.
     */
    @Override
    public void handle(ChannelHandlerContext ctx, AuthMessage msg) {
        if(!msg.getLoginUser().equals("none")){
            openCloudWindow(msg);
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
