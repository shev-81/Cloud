package com.cloud.clientpak.services;

import com.cloud.clientpak.interfaces.ChangeInterface;
import com.cloud.clientpak.Connection;
import com.cloud.clientpak.Controller;
import io.netty.channel.ChannelFuture;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
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

/**
 * The class responsible for sending files to the cloud. When
 * sending a message with file data (size does not exceed 10 mb.), the file
 * cut into equal parts of 10 mb. and it is sent over the network in parts.
 * How many parts the file was cut into, the same number of messages will be
 * sequentially sent to the network. An important point of compliance with the order
 * sending such messages is synchronized in the sync() method.
 * {@link ChannelFuture ChannelFuture}
 */
@Data
@Log4j2
public class FileWorker {

    /**
     * The maximum size of the transmitted message is 10 mb.
     */
    private final int BUFFER_SIZE = 1024 * 1024 * 10;

    /**
     * Check whether the file needs to be overwritten.
     */
    private boolean reWriteFileCheck;

    /**
     * Contains the number of bytes that the cloud is filled with.
     */
    private long lastLoadSizeFiles;

    /**
     * Network connection.
     */
    private Connection connection;

    /**
     * Thread pool.
     */
    private ExecutorService executorService;

    /**
     * The constructor determines from the thread pool 1 the next thread to send messages,
     * one thread is required due to the asynchronous nature of sending messages.
     * If 2 or more parallel threads work, the principle will be violated
     * asynchronous operation at Netty. And there will be confusion in the messages, which to
     * which file it belongs to.
     * @see ExecutorService
     */
    public FileWorker() {
        this.reWriteFileCheck = false;
        this.executorService = Executors.newSingleThreadExecutor();
        this.connection = Controller.getConnection();
    }

    /**
     * Checks the file for availability in the cloud, if there is, then checks with the user
     * whether it needs to be overwritten. Checks whether the storage limit will be exceeded
     * data in the cloud after writing a file (If there is not enough space, it issues a message
     * to the user about the lack of space).
     * @param file a link to the file to be transferred to the cloud.
     * @param changeInterface CallBack for the user's GUI.
     * @param reWrite tells whether the file will be overwritten in the cloud.
     */
    public void checkAddFile(File file, ChangeInterface changeInterface, boolean reWrite) {
        if (reWrite) {
            Alert allert = new Alert(Alert.AlertType.CONFIRMATION, "Файл будет перезаписан.");
            Optional<ButtonType> option = allert.showAndWait();
            if (option.isEmpty()) {
                return;
            } else if (option.get() == ButtonType.OK) {
                reWriteFileCheck = true;
                send(file, changeInterface);
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
        send(file, changeInterface);
    }

    /**
     * If the file is larger than 10 mb, it cuts it into pieces and sends it to the network. After sending each message,
     * CallBack is called to change the user's GUI.
     * @param file a link to the file to be transferred to the cloud.
     * @param changeInterface CallBack for the user's GUI.
     */
    public void send(File file, ChangeInterface changeInterface) {
        this.connection = Controller.getConnection();
        executorService.execute(() -> {
            try {
                int partsCount = (int) (file.length() / BUFFER_SIZE);
                if (file.length() % BUFFER_SIZE != 0) {
                    partsCount++;
                }
                Platform.runLater(() -> changeInterface.call(true));
                FileMessage fmOut = new FileMessage(file.getName(), -1, partsCount, new byte[BUFFER_SIZE]);
                FileInputStream in = new FileInputStream(file);
                for (int i = 0; i < partsCount; i++) {
                    int readedBytes = in.read(fmOut.data);
                    fmOut.partNumber = i + 1;
                    if (readedBytes < BUFFER_SIZE) {
                        fmOut.data = Arrays.copyOfRange(fmOut.data, 0, readedBytes);
                    }
                    ChannelFuture f = connection.send(fmOut);
                    f.sync();
                    log.info("Отправлена часть #" + (i + 1));
                }
                reWriteFileCheck = false;
                ChannelFuture f = connection.send(new FilesSizeRequest(1));
                f.sync();
                in.close();
                Platform.runLater(() -> changeInterface.call(false));
            } catch (IOException | InterruptedException e) {
                log.error(e.toString());
            }
        });
    }

    public void stop() {
        executorService.shutdownNow();
        if(executorService.isShutdown()){
            log.info("Экзекутор сервис отключен");
        }
    }
}
