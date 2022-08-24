package com.cloud.clientpak.handlers;

import com.cloud.clientpak.Controller;
import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import lombok.extern.log4j.Log4j2;
import messages.FileMessage;
import messages.FilesSizeRequest;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Класс слушатель сообщений несущих данные файла. Определяет метод по работе
 * с сообщением содержащим данные файла.
 */
@Log4j2
public class FileHandler {

    /**
     * Переменная {@link Controller Controller}
     */
    private Controller controller;

    /**
     * Поток вывода в файл.
     */
    private FileOutputStream fos;

    /**
     * Маркер определяющий режим записи в файл
     * (нужно ли дописывать данные в конец файла или перезаписать.)
     */
    private boolean append;

    /**
     * Конструктор сохраняет ссылку на контроллер приложения.
     * @param controller контроллер приложения.
     */
    public FileHandler(Controller controller) {
        this.controller = controller;
        this.fos = null;
    }

    /**
     * Получает сообщение с данными файла, проверяет уместился ли файл в 1 сообщение.
     * Если нет то устанавливает маркер "append" в режим "true" для записи данных из
     * последующих сообщений в конец файла. По получению последней части файла в сообщении
     * закрывает поток вывода в файл, и отправляет запрос на обновление данных списка файлов
     * @param ctx channel context.
     * @param msg объект сообщение.
     */
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
            log.info(fmsg.partNumber + " / " + fmsg.partsCount);
            fos.write(fmsg.data);
            if (fmsg.partNumber == fmsg.partsCount) {
                fos.close();
                append = false;
                log.info("файл полностью получен");
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
