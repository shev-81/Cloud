package com.cloud.serverpak.handlers;

import com.cloud.serverpak.services.FilesInformService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import messages.FilesSizeRequest;
import java.io.IOException;

/**
 * The message listener class, defines the response to the request (Files Size Request), about receiving
 * the state of the Cloud for the user who sent the request.
 * @see FilesSizeRequest
 */
@Log4j2
@Handler
public class FilesListRequestHandler extends AbstractHandler<FilesSizeRequest> {

    /**
     * Netty's main listener.
     * @see MainHandler
     */
    private final MainHandler mainHandler;

    /**
     * File information service.
     * @see FilesInformService
     */
    private final FilesInformService fileService;


    /**
     * The constructor saves a reference to the main listener.
     * @param mainHandler Netty's main listener.
     */
    public FilesListRequestHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.fileService = mainHandler.getFilesInformService();
    }

    /**
     * Sends a list of files in the cloud to the user.
     * @param ctx channel context.
     * @param msg the message object.
     */
    @Override
    public void handle(ChannelHandlerContext ctx, FilesSizeRequest msg) {
        String userName = mainHandler.getUserName();
        try {
            ctx.writeAndFlush(new FilesSizeRequest(fileService.getFilesSize(userName), fileService.getListFiles(userName)));
            log.info("По запросу пользователя "+userName+ "выслан список файлов.");
        } catch (IOException e) {
            log.error(e.toString());
        }
    }
}
