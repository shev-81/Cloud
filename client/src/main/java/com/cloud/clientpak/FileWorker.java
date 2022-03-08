package com.cloud.clientpak;

import io.netty.channel.ChannelFuture;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.Data;
import messages.FileMessage;
import messages.FilesSizeRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.cloud.clientpak.Controller.CAPACITY_CLOUD_IN_GB;

@Data
public class FileWorker {

    private boolean reWriteFileCheck;
    private long lastLoadSizeFiles;
    private Connection connection;
    private ChangeInterface changeInterface;
    private ExecutorService executorService;

    public FileWorker() {
        this.reWriteFileCheck = false;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void working(File file, ChangeInterface changeInterface, boolean reWrite) {
        this.connection = Controller.getConnection();
        this.changeInterface = changeInterface;
        if (reWrite) {
            Alert allert = new Alert(Alert.AlertType.CONFIRMATION, "Файл будет перезаписан.");
            Optional<ButtonType> option = allert.showAndWait();
            if (option.get() == null) {
                return;
            } else if (option.get() == ButtonType.OK) {
                reWriteFileCheck = true;
                addFile(file);
                return;
            } else if (option.get() == ButtonType.CANCEL) {
                return;
            }
        }
        if (lastLoadSizeFiles + file.length() > CAPACITY_CLOUD_IN_GB * 1024 * 1024 * 1024) {
            Alert allert = new Alert(Alert.AlertType.INFORMATION, "Нехватает места в облаке для сохранения файла.");
            allert.show();
            return;
        }
        reWriteFileCheck = false;
        addFile(file);
    }

    public void addFile(File file) {
        executorService.execute(() -> {
            try {
                int bufSize = 1024 * 1024 * 10;
                int partsCount = (int) (file.length() / bufSize);
                if (file.length() % bufSize != 0) {
                    partsCount++;
                }
                Platform.runLater(() -> {
                    changeInterface.call(true);
                });
                FileMessage fmOut = new FileMessage(file.getName(), -1, partsCount, new byte[bufSize]);
                FileInputStream in = new FileInputStream(file);
                for (int i = 0; i < partsCount; i++) {
                    int readedBytes = in.read(fmOut.data);
                    fmOut.partNumber = i + 1;
                    if (readedBytes < bufSize) {
                        fmOut.data = Arrays.copyOfRange(fmOut.data, 0, readedBytes);
                    }
                    ChannelFuture f = connection.send(fmOut);
                    f.sync();
                    System.out.println("Отправлена часть #" + (i + 1));
                }
                reWriteFileCheck = false;
                ChannelFuture f = connection.send(new FilesSizeRequest(1));
                f.sync();
                in.close();
                Platform.runLater(() -> {
                    changeInterface.call(false);
                });
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void stop() {
        executorService.shutdown();
    }
}
