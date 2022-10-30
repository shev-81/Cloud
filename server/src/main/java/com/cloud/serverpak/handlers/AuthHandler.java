package com.cloud.serverpak.handlers;

import com.cloud.serverpak.interfaces.AuthService;
import com.cloud.serverpak.services.FilesInformService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import messages.AuthMessage;
import messages.FilesSizeRequest;

import java.io.IOException;

/**
 * Class listener of messages carrying information about the authorization request.
 * Defines the method for user authorization on the server.
 */
@Log4j2
@Handler
public class AuthHandler extends AbstractHandler <AuthMessage>{

    /**
     * Netty's main listener.
     * @see MainHandler
     */
    private final MainHandler mainHandler;

    /**
     * Authorization service.
     * @see AuthService
     */
    private final AuthService authService;

    /**
     * File information service.
     * @see FilesInformService
     */
    private final FilesInformService fileService;

    /**
     * The constructor gets a reference to the main listener.
     * @param mainHandler Netty's main listener.
     */
    public AuthHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.authService = mainHandler.getAuthService();
        this.fileService = mainHandler.getFilesInformService();
    }

    /**
     * Performs user authorization on the server. Requests the user authorization service
     * with the login and password received in the message, if the user is, then sends a service message
     * a message {@link AuthMessage AuthMessage} with the name of the authorized user and a list
     * of files in the cloud for this user. Additionally sends the following message
     * {@link FilesSizeRequest FilesSizeRequest} carrying information about the storage status for
     * this user.If registration is refused, it returns the same service message with
     * Login "none", which indicates to the client that authorization has not been completed.
     * @param ctx channel context.
     * @param msg the message object.
     * @see AuthMessage
     * @see FilesSizeRequest
     */
    @Override
    public void handle(ChannelHandlerContext ctx, AuthMessage msg) {
        String name = msg.getLoginUser();
        String pass = msg.getPassUser();
        String userName = authService.getNickByLoginPass(name, pass);
        if (userName != null) {
            mainHandler.setUserName(userName);
            mainHandler.getChannels().add(ctx.channel());
            try {
                ctx.writeAndFlush(new AuthMessage(userName, fileService.getListFiles(userName)));
                ctx.writeAndFlush(new FilesSizeRequest(fileService.getFilesSize(userName), fileService.getListFiles(userName)));
            } catch (IOException e) {
                log.error(e.toString());
            }
            log.info("Авторизация пройдена успешно выслан список файлов на сервере");
        } else {
            ctx.writeAndFlush(new AuthMessage("none", ""));
            log.info("Авторизация НЕ пройдена.");
        }
    }

    @Override
    public AuthMessage getGeneric() {
        return new AuthMessage();
    }
}
