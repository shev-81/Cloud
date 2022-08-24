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

@Log4j2
public class DelFileHandler{

    private MainHandler mainHandler;
    private FilesInformService fileService;

    public DelFileHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.fileService = mainHandler.getFilesInformService();
    }

    public void delHandle(ChannelHandlerContext ctx, Object msg) {
        String userName = mainHandler.getUserName();
        String nameDelFile = ((DelFileRequest) msg).getNameFile();
        Path path = Paths.get("server/files/" + userName + "/" + nameDelFile);
        try {
            Files.delete(path);
            log.info("Пользователь " + userName + " удалил файл " + nameDelFile);
            ctx.writeAndFlush(new FilesSizeRequest(fileService.getFilesSize(userName), fileService.getListFiles(userName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
