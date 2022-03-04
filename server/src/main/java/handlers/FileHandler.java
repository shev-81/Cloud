package handlers;

import com.cloud.serverpak.FilesInformService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import messages.FileMessage;
import messages.FilesSizeRequest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Log4j2
public class FileHandler {

    private MainHandler mainHandler;
    private FilesInformService fileService;

    public FileHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.fileService = mainHandler.getFilesInformService();
    }

    public void fileHandle(ChannelHandlerContext ctx, Object msg) {
        String userName = mainHandler.getUserName();
        FileMessage fMsg = (FileMessage) msg;
        boolean append = true;
        try {
            if (Files.exists(Paths.get("server/files/" + userName + "/" + fMsg.filename)) && fMsg.partNumber == 1) {
                Files.delete(Paths.get("server/files/" + userName + "/" + fMsg.filename));
            }
            if (fMsg.partsCount == 1) {
                append = false;
            }
            System.out.println(fMsg.partNumber + " / " + fMsg.partsCount);
            FileOutputStream fos = null;
            fos = new FileOutputStream("server/files/" + userName + "/" + fMsg.filename, append);
            fos.write(fMsg.data);
            fos.close();
            if (fMsg.partNumber == fMsg.partsCount) {
                log.info("Файл полностью получен");
            }
            ctx.writeAndFlush(new FilesSizeRequest(
                    fileService.getFilesSize(userName),
                    fileService.getListFiles(userName),
                    fMsg.partNumber,
                    fMsg.partsCount)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
