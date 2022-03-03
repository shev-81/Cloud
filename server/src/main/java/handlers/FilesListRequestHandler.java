package handlers;

import com.cloud.serverpak.FilesInformService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelHandlerContext;
import messages.FilesSizeRequest;

import java.io.IOException;

public class FilesListRequest implements RequestHandler{

    private MainHandler mainHandler;
    private FilesInformService fileService;

    public FilesListRequest(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.fileService = mainHandler.getFilesInformService();
    }

    @Override
    public void handle(ChannelHandlerContext ctx, Object msg) {
        String userName = mainHandler.getUserName();
        try {
            ctx.writeAndFlush(new FilesSizeRequest(fileService.getFilesSize(userName), fileService.getListFiles(userName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
