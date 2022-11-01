package com.cloud.serverpak.handlers;

import com.cloud.serverpak.MainHandler;
import com.cloud.serverpak.interfaces.RequestHandler;
import com.cloud.serverpak.services.ServiceLocator;
import lombok.Data;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The message listener logger class.
 */
@Data
public class RegistryHandler {

    /**
     * A variable with a listener map.
     */
    private Map<Class<?>, AbstractHandler<?>> mapHandlers;

    private ServiceLocator serviceLocator;

    /**
     * Creates a collection {@link HashMap HashMap}, puts it in the form of keys
     * message classes, as well as listener methods for processing these messages.
     * @param mainHandler is the main message listener.
     */
    public RegistryHandler(MainHandler mainHandler) {
        this.mapHandlers = new HashMap<>();
        this.serviceLocator = mainHandler.getServiceLocator();
        try {
            regHandlers(mainHandler);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the listener method from the {@link HashMap HashMap}
     * collection, by the class of the received message.
     * @param cl Message class.
     * @return the listener method in the interface variable.
     */
    public RequestHandler<?> getHandler(Class<?> cl) {
        return mapHandlers.get(cl);
    }

    private void regHandlers(MainHandler mainHandler) throws Exception{
        List<Constructor<?>> constructorHandlers = serviceLocator.getListConstructors();
        for(Constructor<?>  constructor: constructorHandlers){
            AbstractHandler<?> handler = (AbstractHandler<?>)constructor.newInstance(mainHandler);
            mapHandlers.put(handler.getGeneric(), handler);
        }
    }
}
