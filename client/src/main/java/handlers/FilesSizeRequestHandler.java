package handlers;

import com.cloud.clientpak.Controller;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import lombok.extern.log4j.Log4j2;
import messages.FileInfo;
import messages.FilesSizeRequest;

import java.util.List;

@Log4j2
public class FilesSizeRequestHandler {

    private Controller controller;
    private boolean check;
    private double percentProgBar;
    private double partsCount;
    private double partNumber;


    public FilesSizeRequestHandler(Controller controller) {
        this.controller = controller;
        this.check = false;
    }

    public void filesSizeReqHandle(ChannelHandlerContext ctx, Object msg) {
        if (controller.getFileWorker().isReWriteFileCheck()) {
            return;
        }
        FilesSizeRequest filesObj = (FilesSizeRequest) msg;
        controller.getFileWorker().setLastLoadSizeFiles(filesObj.getFilesSize());
        double filesSize = (double) filesObj.getFilesSize() / 1024 / 1024 / 1024;
        List<FileInfo> listFiles = filesObj.getListFiles();
        controller.setFileList(listFiles);
        partsCount = filesObj.getPartsCount();
        partNumber = filesObj.getPartNumber();
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
