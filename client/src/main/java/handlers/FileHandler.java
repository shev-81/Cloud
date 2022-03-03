package handlers;

import com.cloud.clientpak.Controller;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import messages.FileMessage;
import messages.FilesSizeRequest;

import java.io.FileOutputStream;
import java.io.IOException;

public class FileHandler{

    private Controller controller;

    public FileHandler(Controller controller) {
        this.controller = controller;
    }

    public void fileHandle(ChannelHandlerContext ctx, Object msg) {
        FileMessage fmsg = (FileMessage) msg;
        boolean append = true;
        if (fmsg.partsCount == 1) {
            append = false;
        }
        double percentProgressBar = (double) 1 / fmsg.partsCount;
        Platform.runLater(() -> {
            controller.getFileNameMessage().setText("Копируем файл - "+ fmsg.filename + ".");
            controller.getFileNameMessage().setVisible(true);
            controller.getProgressBar().setVisible(true);
            controller.getProgressBar().setProgress((double) fmsg.partNumber * percentProgressBar);
        });
        System.out.println(fmsg.partNumber + " / " + fmsg.partsCount);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("client/files/" + fmsg.filename, append);
            fos.write(fmsg.data);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fmsg.partNumber == fmsg.partsCount) {
            System.out.println("файл полностью получен");
            ctx.writeAndFlush(new FilesSizeRequest(1));
            Platform.runLater(()->{
                controller.getProgressBar().setVisible(false);
                controller.getFileNameMessage().setVisible(false);
            });
        }
    }
}
