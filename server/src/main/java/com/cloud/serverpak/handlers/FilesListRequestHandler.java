package com.cloud.serverpak.handlers;

import com.cloud.serverpak.services.FilesInformService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import messages.FilesSizeRequest;
import java.io.IOException;

/**
 * Класс слушатель сообщений, определяет реакцию на запрос (FilesSizeRequest), о получении
 * состоянии Облака для пользователя пославшего запрос.
 * @see FilesSizeRequest
 */
@Log4j2
public class FilesListRequestHandler{

    /**
     * Главный слушатель Netty.
     * @see MainHandler
     */
    private MainHandler mainHandler;

    /**
     * Файловый информационный сервис.
     * @see FilesInformService
     */
    private FilesInformService fileService;


    /**
     * Конструктор сохраняет ссылку на главный слушатель.
     * @param mainHandler
     */
    public FilesListRequestHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.fileService = mainHandler.getFilesInformService();
    }

    /**
     * Высылает список файлов в облаке для пользователя.
     * @param ctx channel context.
     * @param msg the message object.
     */
    public void filesListHandle(ChannelHandlerContext ctx, Object msg) {
        String userName = mainHandler.getUserName();
        try {
            ctx.writeAndFlush(new FilesSizeRequest(fileService.getFilesSize(userName), fileService.getListFiles(userName)));
            log.info("По запросу пользователя "+userName+ "выслан список файлов.");
        } catch (IOException e) {
            log.error(e.toString());
        }
    }
}
