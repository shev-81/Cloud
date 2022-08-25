package com.cloud.serverpak.handlers;

import com.cloud.serverpak.services.FilesInformService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import messages.FileMessage;
import messages.FilesSizeRequest;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Класс слушатель сообщений несущих данные файла. Определяет метод по работе
 * с сообщением содержащим данные файла.
 */
@Log4j2
public class FileHandler {

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
     * Поток вывода в файл.
     */
    private FileOutputStream fos;

    /**
     * Маркер определяющий режим записи в файл
     * (нужно ли дописывать данные в конец файла или перезаписать.)
     */
    private boolean append;

    /**
     * Конструктор сохраняет ссылку главный слушатель.
     * @param mainHandler
     */
    public FileHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.fileService = mainHandler.getFilesInformService();
    }

    /**
     * Получает сообщение с данными файла, проверяет уместился ли файл в 1 сообщение.
     * Если нет то устанавливает маркер "append" в режим "true" для записи данных из
     * последующих сообщений в конец файла. По получению последней части файла в сообщении
     * закрывает поток вывода в файл. Отправляет клиенту служебное сообщение с обновленными
     * данными о состоянии хранилища.
     * @see FilesSizeRequest
     * @param ctx channel context.
     * @param msg объект сообщение.
     */
    public void fileHandle(ChannelHandlerContext ctx, Object msg) {
        String userName = mainHandler.getUserName();
        FileMessage fmsg = (FileMessage) msg;
        try {
            if (fmsg.partNumber == 1) {
                append = false;
                fos = null;
                fos = new FileOutputStream("server/files/" + userName + "/" + fmsg.filename, append);
            } else {
                append = true;
            }
            log.info(fmsg.partNumber + " / " + fmsg.partsCount);
            fos.write(fmsg.data);
            if (fmsg.partNumber == fmsg.partsCount) {
                fos.close();
                append = false;
                log.info("Файл полностью получен");
            }
            ctx.writeAndFlush(new FilesSizeRequest(
                    fileService.getFilesSize(userName),
                    fileService.getListFiles(userName),
                    fmsg.partNumber,
                    fmsg.partsCount)
            );
        } catch (IOException e) {
            log.error(e.toString());
        }
    }
}
