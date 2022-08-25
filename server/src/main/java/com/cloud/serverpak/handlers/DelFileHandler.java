package com.cloud.serverpak.handlers;

import com.cloud.serverpak.services.FilesInformService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import messages.DelFileRequest;
import messages.FilesSizeRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Класс слушатель сообщений несущих информацию об удалении файла. Определяет метод по работе
 * с сообщением представляющим запрос на удаление файла из облака.
 */
@Log4j2
public class DelFileHandler{

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
     * Конструктор получает ссылку на галвный слушатель.
     * @param mainHandler Главный слушатель Netty.
     */
    public DelFileHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.fileService = mainHandler.getFilesInformService();
    }

    /**
     * Удаляет файл с облака у пользователя.
     * @param ctx channel context.
     * @param msg the message object.
     * @see DelFileRequest
     */
    public void delHandle(ChannelHandlerContext ctx, Object msg) {
        String userName = mainHandler.getUserName();
        String nameDelFile = ((DelFileRequest) msg).getNameFile();
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
