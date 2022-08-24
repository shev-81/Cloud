package com.cloud.clientpak.handlers;

import com.cloud.clientpak.Controller;
import com.cloud.clientpak.interfaces.RequestHandler;
import lombok.Data;
import messages.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс регистратор слушателей сообщений.
 */
@Data
public class RegistryHandler {

    /**
     * Переменная с картой слушателей.
     */
    private Map<Class<? extends AbstractMessage>, RequestHandler> mapHandlers;

    /**
     * Создает коллекцию {@link HashMap HashMap}, помещает в нее в виде ключей
     * классы сообщений, а значения методы слушателей для обработки этих сообщений.
     * @param controller  контроллер приложения.
     */
    public RegistryHandler(Controller controller) {
        this.mapHandlers = new HashMap<>();
        mapHandlers.put(AuthMessage.class, new AuthHandler(controller)::authHandle);
        mapHandlers.put(RegUserRequest.class, new RegUserHandler(controller)::regHandle);
        mapHandlers.put(FileMessage.class, new FileHandler(controller)::fileHandle);
        mapHandlers.put(FilesSizeRequest.class, new FilesSizeRequestHandler(controller)::filesSizeReqHandle);
    }

    /**
     * Возвращает из коллекции {@link HashMap HashMap} метод слушателя, по классу пришедшего сообщения.
     * @param cl Класс сообщения
     * @return метод слушателя в интерфейсной переменной.
     */
    public RequestHandler getHandler(Class cl) {
        return mapHandlers.get(cl);
    }
}
