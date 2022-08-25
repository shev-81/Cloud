package com.cloud.serverpak.handlers;

import com.cloud.serverpak.services.FilesInformService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import messages.FileMessage;
import messages.FileRequest;
import messages.FilesSizeRequest;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

/**
 * Класс слушатель сообщений запросов файлов. Определяет метод по работе
 * с сообщением содержащим данные файла.
 */
@Data
@Log4j2
public class ReqFileHandler {

    /**
     * Буфер масива байт для объекта сообщения.
     */
    private static final int BUF_SIZE = 1024 * 1024 * 10;

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
     * Ссылка на пулл потоков из главного слушателя.
     */
    private ExecutorService executorService;

    /**
     * Конструктор сохраняет ссылку на главный слушатель сообщений.
     * @see FilesInformService
     * @see ExecutorService
     * @param mainHandler
     */
    public ReqFileHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.fileService = mainHandler.getFilesInformService();
        this.executorService = mainHandler.getExecutorService();
    }

    /**
     * Обрабатывает запрос от клиента на получение файла, если файл весит больше 10 mb,
     * разрезает его на части по 10 mb. и отправляет клиенту.
     * @param ctx channel context.
     * @param msg объект сообщение.
     */
    public void reqFileHandle(ChannelHandlerContext ctx, Object msg) {
        executorService.execute(() -> {
            try {
                String userName = mainHandler.getUserName();
                String nameFile = ((FileRequest) msg).getFilename();
                File file = new File("server/files/" + userName + "/" + nameFile);
                int partsCount = (int) (file.length() / BUF_SIZE);
                if (file.length() % BUF_SIZE != 0) {
                    partsCount++;
                }
                FileMessage fmOut = new FileMessage(nameFile, -1, partsCount, new byte[BUF_SIZE]);
                FileInputStream in = new FileInputStream(file);
                for (int i = 0; i < partsCount; i++) {
                    int readedBytes = in.read(fmOut.data);
                    fmOut.partNumber = i + 1;
                    if (readedBytes < BUF_SIZE) {
                        fmOut.data = Arrays.copyOfRange(fmOut.data, 0, readedBytes);
                    }
                    ChannelFuture f = ctx.writeAndFlush(fmOut);
                    f.sync();
                    log.info("Отправлена часть #" + (i + 1));
                }
                in.close();
                ChannelFuture f = ctx.writeAndFlush(new FilesSizeRequest(fileService.getFilesSize(userName), fileService.getListFiles(userName)));
                f.sync();
            } catch (Exception e) {
                log.error(e.toString());
            }
        });
    }
}
