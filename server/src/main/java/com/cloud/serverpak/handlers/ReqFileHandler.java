package com.cloud.serverpak.handlers;

import com.cloud.serverpak.interfaces.RequestHandler;
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
 * File request message listener class. Defines a method for work
 * with a message containing the file data.
 */
@Data
@Log4j2
@Handler
public class ReqFileHandler extends AbstractHandler<FileRequest> {

    /**
     * Byte array buffer for the message object.
     */
    private static final int BUF_SIZE = 1024 * 1024 * 10;

    /**
     * Netty's main listener.
     * @see MainHandler
     */
    private MainHandler mainHandler;

    /**
     * File information service.
     * @see FilesInformService
     */
    private FilesInformService fileService;

    /**
     * A link to the thread pool from the main listener.
     */
    private ExecutorService executorService;

    /**
     * The constructor saves a link to the main message listener.     * @see FilesInformService
     * @see ExecutorService
     * @param mainHandler Netty's main listener.
     */
    public ReqFileHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.fileService = mainHandler.getFilesInformService();
        this.executorService = mainHandler.getExecutorService();
    }

    /**
     * Processes a request from the client to receive a file if the file weighs more than 10 mb,
     * cuts it into 10 mb pieces. and sends it to the client.
     * @param ctx channel context.
     * @param msg the message object.
     */
    @Override
    public void handle(ChannelHandlerContext ctx, FileRequest msg) {
        executorService.execute(() -> {
            try {
                String userName = mainHandler.getUserName();
                String nameFile = msg.getFilename();
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
