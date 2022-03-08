package handlers;

import com.cloud.serverpak.FilesInformService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import messages.FileMessage;
import messages.FileRequest;
import messages.FilesSizeRequest;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReqFileHandler {

    private MainHandler mainHandler;
    private FilesInformService fileService;
    private ExecutorService executorService;

    public ReqFileHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.fileService = mainHandler.getFilesInformService();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void reqFileHandle(ChannelHandlerContext ctx, Object msg) {
        executorService.execute(() -> {
            try {
                String userName = mainHandler.getUserName();
                String nameFile = ((FileRequest) msg).getFilename();
                File file = new File("server/files/" + userName + "/" + nameFile);
                int bufSize = 1024 * 1024 * 10;
                int partsCount = (int) (file.length() / bufSize);
                if (file.length() % bufSize != 0) {
                    partsCount++;
                }
                FileMessage fmOut = new FileMessage(nameFile, -1, partsCount, new byte[bufSize]);
                FileInputStream in = new FileInputStream(file);
                for (int i = 0; i < partsCount; i++) {
                    int readedBytes = in.read(fmOut.data);
                    fmOut.partNumber = i + 1;
                    if (readedBytes < bufSize) {
                        fmOut.data = Arrays.copyOfRange(fmOut.data, 0, readedBytes);
                    }
                    ChannelFuture f = ctx.writeAndFlush(fmOut);
                    f.sync();
                    System.out.println("Отправлена часть #" + (i + 1));
                }
                in.close();
                ChannelFuture f = ctx.writeAndFlush(new FilesSizeRequest(fileService.getFilesSize(userName), fileService.getListFiles(userName)));
                f.sync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
