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
 * Клас отвечающий за работу по отправке файлов в облако. При выполнении
 * отправки сообщения с данными файла (размер не превышает 10 mb.), файл
 * режется на равные части по 10 mb. и отправляется по сети частями. На
 * сколько частей был разрезан файл, столько же сообщений будет
 * последовательно отправлено в сеть. Важный момент соблюдения очередности
 * отправки таких сообщений синхронизируется в методе sync()
 * {@link ChannelFuture ChannelFuture}
 */
@Data
@Log4j2
public class FileWorker {

    /**
     * Максимальный размер передаваемого сообщения 10mb.
     */
    private final int BUFFER_SIZE = 1024 * 1024 * 10;

    /**
     * Чек, нужна ли перезапись файла.
     */
    private boolean reWriteFileCheck;

    /**
     * Содержит количество байт на которое наполнено облако.
     */
    private long lastLoadSizeFiles;

    /**
     * Сетевое соединение.
     */
    private Connection connection;

    /**
     * Пулл потоков.
     */
    private ExecutorService executorService;

    /**
     * Конструктор определяет из пула потоков 1 раобчий поток для отправки сообщений,
     * один поток необходим по причине соблюдения асинхронности отправки сообщений.
     * Если будет работать 2 и более паралельных потоков то будет нарушен принцип
     * асинхронности работы у Netty. И возникнет путаница в сообщениях, какое к
     * какому файлу относится.
     * @see ExecutorService
     */
    public FileWorker() {
        this.reWriteFileCheck = false;
        this.executorService = Executors.newSingleThreadExecutor();
        this.connection = Controller.getConnection();
    }

    /**
     * Проверяет файл на наличие в облаке, если есть то уточняет у пользователя
     * нужно ли его перезаписать. Проверяет будет ли превышен лимит на хранение
     * данных в облаке после записи файла (Если нехватает места выдает сообщение
     * пользователю о нехватке места).
     * @param file ссылка на файл подлежащий передаче в облако.
     * @param changeInterface CallBack для GUI пользователя.
     * @param reWrite говорит будет ли файл перезаписан в облаке.
     */
    public void checkAddFile(File file, ChangeInterface changeInterface, boolean reWrite) {
        if (reWrite) {
            Alert allert = new Alert(Alert.AlertType.CONFIRMATION, "Файл будет перезаписан.");
            Optional<ButtonType> option = allert.showAndWait();
            if (option.get() == null) {
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
     * Если файл больше 10 mb режет его на части и отправляет в сеть. После отправки каждого сообщения,
     * вызывается CallBack для изменения GUI пользователя.
     * @param file ссылка на файл подлежащий передаче в облако.
     * @param changeInterface CallBack для GUI пользователя.
     */
    public void send(File file, ChangeInterface changeInterface) {
        this.connection = Controller.getConnection();
        executorService.execute(() -> {
            try {
                int partsCount = (int) (file.length() / BUFFER_SIZE);
                if (file.length() % BUFFER_SIZE != 0) {
                    partsCount++;
                }
                Platform.runLater(() -> {
                    changeInterface.call(true);
                });
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
                Platform.runLater(() -> {
                    changeInterface.call(false);
                });
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
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
