package com.cloud.serverpak.handlers;

import com.cloud.serverpak.interfaces.AuthService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelHandlerContext;
import messages.RegUserRequest;

/**
 * Класс слушатель сообщений {@link RegUserRequest RegUserRequest}.
 */
public class RegUserHandler{

    /**
     * Сервис авторизации.
     */
    private AuthService authService;

    /**
     * Конструктор сохранят ссылку на главный слушатель.
     * @param mainHandler
     */
    public RegUserHandler(MainHandler mainHandler) {
        this.authService = mainHandler.getAuthService();
    }

    /**
     * Обрабатывает служебное сообщение - запрос на регистрацию нового пользователя,
     * проверяет есть ла пользователь с таким же ником в БД, если есть, то отправляет
     * отказ в регистрации, а при отсутствии регистрирует его и посылает ответ клиенту,
     * что регистрация прошла успешно.
     * @param ctx channel context.
     * @param msg объект сообщение.
     */
    public void regHandle(ChannelHandlerContext ctx, Object msg) {
        RegUserRequest regMsg = (RegUserRequest) msg;
        if (regMsg.getNameUser().equals(authService.getNickByLoginPass(regMsg.getLogin(), regMsg.getPassUser()))) {
            ctx.writeAndFlush(new RegUserRequest("none", "", ""));
        } else {
            if (authService.registerNewUser(regMsg.getNameUser(), regMsg.getLogin(), regMsg.getPassUser())) {
                ctx.writeAndFlush(new RegUserRequest("reg", "", ""));
            }
        }
    }
}
