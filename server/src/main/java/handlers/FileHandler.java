package handlers;

import com.cloud.serverpak.FilesInformService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import messages.FileMessage;
import messages.FilesSizeRequest;

import java.io.FileOutputStream;
import java.io.IOException;

@Log4j2
public class FileHandler {

    private MainHandler mainHandler;
    private FilesInformService fileService;
    private FileOutputStream fos;
    private boolean append;

    public FileHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.fileService = mainHandler.getFilesInformService();
    }

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
            System.out.println(fmsg.partNumber + " / " + fmsg.partsCount);
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
            e.printStackTrace();
        }
    }
}
