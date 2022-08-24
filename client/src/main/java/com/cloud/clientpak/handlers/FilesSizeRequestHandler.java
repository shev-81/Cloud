package com.cloud.clientpak.handlers;

import com.cloud.clientpak.Controller;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import lombok.extern.log4j.Log4j2;
import messages.FileInfo;
import messages.FilesSizeRequest;
import java.util.List;

/**
 * Класс слушатель сообщений {@link FilesSizeRequest FilesSizeRequest}.
 */
@Log4j2
public class FilesSizeRequestHandler {

    /**
     * Переменная {@link Controller Controller}
     */
    private Controller controller;

    /**
     * Метка маркер, необходимая для отображения загрузки файла.
     */
    private boolean check;

    /**
     * Процент прогресс бара.
     */
    private double percentProgBar;

    /**
     * Общее колличество часте файла.
     */
    private double partsCount;

    /**
     * Номер текущей части файла.
     */
    private double partNumber;


    /**
     * Конструктор сохраняет ссылку на контроллер приложения.
     * @param controller контроллер приложения.
     */
    public FilesSizeRequestHandler(Controller controller) {
        this.controller = controller;
        this.check = false;
    }

    /**
     * Получив от сервера ответ о данных в облаке, обновляет
     * все данные в GUI о состоянии облака.
     * @param ctx channel context.
     * @param msg объект сообщение.
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
