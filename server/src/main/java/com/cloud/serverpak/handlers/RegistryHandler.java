package com.cloud.serverpak.handlers;

import com.cloud.serverpak.MainHandler;
import com.cloud.serverpak.interfaces.RequestHandler;
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
     * @param mainHandler главный слушатель сообщений.
     */
    public RegistryHandler(MainHandler mainHandler) {
        this.mapHandlers = new HashMap<>();
        mapHandlers.put(AuthMessage.class, new AuthHandler(mainHandler)::authHandle);
        mapHandlers.put(RegUserRequest.class, new RegUserHandler(mainHandler)::regHandle);
        mapHandlers.put(FileRequest.class, new ReqFileHandler(mainHandler)::reqFileHandle);
        mapHandlers.put(DelFileRequest.class, new DelFileHandler(mainHandler)::delHandle);
        mapHandlers.put(FileMessage.class, new FileHandler(mainHandler)::fileHandle);
        mapHandlers.put(FilesSizeRequest.class, new FilesListRequestHandler(mainHandler)::filesListHandle);
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
