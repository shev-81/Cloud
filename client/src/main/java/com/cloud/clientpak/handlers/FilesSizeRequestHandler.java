package com.cloud.clientpak.handlers;

import com.cloud.clientpak.Controller;
import com.cloud.clientpak.interfaces.RequestHandler;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import lombok.extern.log4j.Log4j2;
import messages.FileInfo;
import messages.FilesSizeRequest;
import java.util.List;

/**
 * Message Listener class {@link FilesSizeRequest FilesSizeRequest}.
 */
@Log4j2
@Handler
public class FilesSizeRequestHandler extends AbstractHandler <FilesSizeRequest>{

    /**
     * Variable {@link Controller Controller}
     */
    private Controller controller;

    /**
     * The marker label required to display the file upload.
     */
    private boolean check;

    /**
     * Percentage of the progress bar.
     */
    private double percentProgBar;

    /**
     * The constructor saves a reference to the application controller.
     * @param controller application controller.
     */
    public FilesSizeRequestHandler(Controller controller) {
        this.controller = controller;
        this.check = false;
    }


    /**
     * After receiving a response from the server about the data in the cloud, updates
     * all data in the GUI about the state of the cloud.
     * @param ctx channel context.
     * @param msg the message object.
     */
    @Override
    public void handle(ChannelHandlerContext ctx, FilesSizeRequest msg) {
        if (controller.getFileWorker().isReWriteFileCheck()) {
            return;
        }
        controller.getFileWorker().setLastLoadSizeFiles(msg.getFilesSize());
        double filesSize = (double) msg.getFilesSize() / 1024 / 1024 / 1024;
        List<FileInfo> listFiles = msg.getListFiles();
        controller.setFileList(listFiles);
        double partsCount = msg.getPartsCount();
        double partNumber = msg.getPartNumber();
        if(partsCount != partNumber){
            percentProgBar =  (1 / partsCount) * partNumber;
            check = true;
        }else{
            check = false;
        }
        Platform.runLater(() -> {
            controller.changeLoadBar(filesSize);
            controller.getFileSizeLabel().setText(String.valueOf(filesSize).substring(0, 3));
            controller.reloadFxFilesList(listFiles);
            controller.setVisibleLoadInfoFile(check);
            controller.changeProgressBar(percentProgBar);
        });
    }
}
