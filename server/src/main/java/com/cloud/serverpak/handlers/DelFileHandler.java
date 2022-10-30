package com.cloud.serverpak.handlers;

import com.cloud.serverpak.interfaces.RequestHandler;
import com.cloud.serverpak.services.FilesInformService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import messages.DelFileRequest;
import messages.FileRequest;
import messages.FilesSizeRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class listener of messages carrying information about
 * file deletion. Defines a method for work with a message
 * representing a request to delete a file from the cloud.
 */
@Log4j2
public class DelFileHandler implements RequestHandler<DelFileRequest> {

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
     * The constructor gets a reference to the main listener.
     * @param mainHandler Netty's main listener.
     */
    public DelFileHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.fileService = mainHandler.getFilesInformService();
    }

    /**
     * Deletes a file from the user's cloud.
     * @param ctx channel context.
     * @param msg the message object.
     * @see DelFileRequest
     */
    @Override
    public void handle(ChannelHandlerContext ctx, DelFileRequest msg) {
        String userName = mainHandler.getUserName();
        String nameDelFile = msg.getNameFile();
        Path path = Paths.get("server/files/" + userName + "/" + nameDelFile);
        try {
            Files.delete(path);
            log.info("Пользователь " + userName + " удалил файл " + nameDelFile);
            ctx.writeAndFlush(new FilesSizeRequest(fileService.getFilesSize(userName), fileService.getListFiles(userName)));
        } catch (IOException e) {
            log.error(e.toString());
        }
    }
}
