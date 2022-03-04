package handlers;

import com.cloud.clientpak.Controller;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import messages.FileMessage;
import messages.FilesSizeRequest;

import java.io.FileOutputStream;
import java.io.IOException;


public class FileHandler {

    private Controller controller;
    private FileOutputStream fos;
    private boolean append;

    public FileHandler(Controller controller) {
        this.controller = controller;
        this.fos = null;
    }

    public void fileHandle(ChannelHandlerContext ctx, Object msg) {
        FileMessage fmsg = (FileMessage) msg;
        try {
            if (fmsg.partNumber == 1) {
                append = false;
                fos = null;
                fos = new FileOutputStream("client/files/" + fmsg.filename, append);
            } else {
                append = true;
            }
            Platform.runLater(() -> {
                controller.setVisibleLoadInfoFile(true);
                controller.changeProgressBar((double) fmsg.partNumber * ((double) 1 / fmsg.partsCount));
            });
            System.out.println(fmsg.partNumber + " / " + fmsg.partsCount);
            fos.write(fmsg.data);
            if (fmsg.partNumber == fmsg.partsCount) {
                fos.close();
                append = false;
                System.out.println("файл полностью получен");
                ctx.writeAndFlush(new FilesSizeRequest(1));
                Platform.runLater(() -> {
                    controller.setVisibleLoadInfoFile(false);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
