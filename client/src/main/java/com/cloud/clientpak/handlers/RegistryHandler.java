package com.cloud.clientpak.handlers;

import com.cloud.clientpak.Controller;
import com.cloud.clientpak.interfaces.RequestHandler;
import lombok.Data;
import messages.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The message listener logger class.
 */
@Data
public class RegistryHandler {

    /**
     * A variable with a listener map.
     */
    private Map<Class<? extends AbstractMessage>, RequestHandler> mapHandlers;

    //todo добавить сервис локатор, через рефлексию сканирования пакета на наличие хендлеров.

    /**
     * Creates a collection {@link HashMap HashMap}, puts it in the form of keys
     * message classes, as well as listener methods for processing these messages.
     * @param controller application controller.
     */
    public RegistryHandler(Controller controller) {
        this.mapHandlers = new HashMap<>();
        mapHandlers.put(AuthMessage.class, new AuthHandler(controller)::authHandle);
        mapHandlers.put(RegUserRequest.class, new RegUserHandler(controller)::regHandle);
        mapHandlers.put(FileMessage.class, new FileHandler(controller)::fileHandle);
        mapHandlers.put(FilesSizeRequest.class, new FilesSizeRequestHandler(controller)::filesSizeReqHandle);
    }

    /**
     * Returns the listener method from the {@link HashMap HashMap}
     * collection, by the class of the received message.
     * @param cl Message class
     * @return method of the listener in the interface variable.
     */
    public RequestHandler getHandler(Class cl) {
        return mapHandlers.get(cl);
    }
}
