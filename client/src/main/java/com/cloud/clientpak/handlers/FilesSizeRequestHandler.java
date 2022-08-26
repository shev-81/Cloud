package com.cloud.clientpak.handlers;

import com.cloud.clientpak.Controller;
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
public class FilesSizeRequestHandler {

    /**
     * Variable {@link Controller Controller}
     */
    private final Controller controller;

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
    public void filesSizeReqHandle(ChannelHandlerContext ctx, Object msg) {
        if (controller.getFileWorker().isReWriteFileCheck()) {
            return;
        }
        FilesSizeRequest filesObj = (FilesSizeRequest) msg;
        controller.getFileWorker().setLastLoadSizeFiles(filesObj.getFilesSize());
        double filesSize = (double) filesObj.getFilesSize() / 1024 / 1024 / 1024;
        List<FileInfo> listFiles = filesObj.getListFiles();
        controller.setFileList(listFiles);
        double partsCount = filesObj.getPartsCount();
        double partNumber = filesObj.getPartNumber();
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
