package com.cloud.clientpak.handlers;

import com.cloud.clientpak.Controller;
import com.cloud.clientpak.interfaces.RequestHandler;
import lombok.Data;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The message listener logger class.
 */
@Data
public class RegistryHandler{

    private static final String HANDLERS_PACKAGE = "com.cloud.clientpak.handlers";

    private static final String MESSAGES_PACKAGE = "messages";
    /**
     * A variable with a listener map.
     */
    private Map<Class<?>, RequestHandler<?>> mapHandlers;

    /**
     * Creates a collection {@link HashMap HashMap}, puts it in the form of keys
     * message classes, as well as listener methods for processing these messages.
     * @param controller application controller.
     */
    public RegistryHandler(Controller controller) {
        this.mapHandlers = new HashMap<>();
        try {
            regHandlers(controller);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Register the listener method from the {@link HashMap HashMap}
     * collection, by the class of the received message.
     * @param controller
     */
    private void regHandlers(Controller controller) throws Exception{
        List<Constructor<?>> constructorHandlers = controller.getServiceLocator().getListConstructors();
        for(Constructor<?>  constructor: constructorHandlers){
            AbstractHandler<?> handler = (AbstractHandler<?>)constructor.newInstance(controller);
            mapHandlers.put(handler.getGeneric(), handler);
        }
    }

    /**
     * Returns the listener method from the {@link HashMap HashMap}
     * collection, by the class of the received message.
     * @param cl Message class
     * @return method of the listener in the interface variable.
     */
    public RequestHandler getHandler(Class<?> cl) {
        return mapHandlers.get(cl);
    }
}
