package handlers;

import com.cloud.serverpak.FilesInformService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import messages.FilesSizeRequest;

import java.io.IOException;

@Log4j2
public class FilesListRequestHandler{

    private MainHandler mainHandler;
    private FilesInformService fileService;

    public FilesListRequestHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.fileService = mainHandler.getFilesInformService();
    }

    public void filesListHandle(ChannelHandlerContext ctx, Object msg) {
        String userName = mainHandler.getUserName();
        try {
            ctx.writeAndFlush(new FilesSizeRequest(fileService.getFilesSize(userName), fileService.getListFiles(userName)));
            log.info("По запросу пользователя "+userName+ "выслан список файлов.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
