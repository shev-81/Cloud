package com.cloud.serverpak.handlers;

import com.cloud.serverpak.interfaces.AuthService;
import com.cloud.serverpak.services.FilesInformService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import messages.AuthMessage;
import messages.FilesSizeRequest;
import java.io.IOException;

/**
 * Класс слушатель сообщений несущих информацию о запросе авторизации.
 * Определяет метод по авторизации пользователя на сервере.
 */
@Log4j2
public class AuthHandler{

    /**
     * Главный слушатель Netty.
     * @see MainHandler
     */
    private MainHandler mainHandler;

    /**
     * Сервис авторизации.
     * @see AuthService
     */
    private AuthService authService;

    /**
     * Файловый информационный сервис.
     * @see FilesInformService
     */
    private FilesInformService fileService;

    /**
     * Конструктор получает ссылку на галвный слушатель.
     * @param mainHandler Главный слушатель Netty.
     */
    public AuthHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.authService = mainHandler.getAuthService();
        this.fileService = mainHandler.getFilesInformService();
    }

    /**
     * Проводит авторизацию пользователя на сервере. Запрашивает сервис авторизации пользователя
     * с логином и паролем полученным в сообщении, если пользователь есть то посылает служебное
     * сообщение {@link AuthMessage AuthMessage}с именем авторизовавшегося пользователя и списком
     * файлов в облаке для данного пользователя. Дополнительно посылает следом сообщение
     * {@link FilesSizeRequest FilesSizeRequest} несущее информацию о состоянии хранилища для
     * данного пользователя.При отказе регистрации  возвращает такое же служебное сообщение с
     * Логином "none", что свидетельствует для клиента о не пройденной авторизации.
     * @param ctx channel context.
     * @param msg the message object.
     * @see AuthMessage
     * @see FilesSizeRequest
     */
    public void authHandle(ChannelHandlerContext ctx, Object msg) {
        String name = ((AuthMessage) msg).getLoginUser();
        String pass = ((AuthMessage) msg).getPassUser();
        String userName = authService.getNickByLoginPass(name, pass);
        if (userName != null) {
            mainHandler.setUserName(userName);
            mainHandler.getChannels().add(ctx.channel());
            try {
                ctx.writeAndFlush(new AuthMessage(userName, fileService.getListFiles(userName)));
                ctx.writeAndFlush(new FilesSizeRequest(fileService.getFilesSize(userName), fileService.getListFiles(userName)));
            } catch (IOException e) {
                log.error(e.toString());
            }
            log.info("Авторизация пройдена успешно выслан список файлов на сервере");
        } else {
            ctx.writeAndFlush(new AuthMessage("none", ""));
            log.info("Авторизация НЕ пройдена.");
        }
    }
}
