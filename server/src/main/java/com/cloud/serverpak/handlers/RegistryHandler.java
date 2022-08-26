package com.cloud.serverpak.handlers;

import com.cloud.serverpak.MainHandler;
import com.cloud.serverpak.interfaces.RequestHandler;
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

    /**
     * Creates a collection {@link HashMap HashMap}, puts it in the form of keys
     * message classes, as well as listener methods for processing these messages.
     * @param mainHandler is the main message listener.
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
     * Returns the listener method from the {@link HashMap HashMap}
     * collection, by the class of the received message.
     * @param cl Message class.
     * @return the listener method in the interface variable.
     */
    public RequestHandler getHandler(Class cl) {
        return mapHandlers.get(cl);
    }
}
