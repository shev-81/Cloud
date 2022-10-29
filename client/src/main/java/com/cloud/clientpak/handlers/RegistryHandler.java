package com.cloud.clientpak.handlers;

import com.cloud.clientpak.Controller;
import com.cloud.clientpak.interfaces.RequestHandler;
import lombok.Data;
import messages.*;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
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
    private Map<Class<?>, RequestHandler> mapHandlers;

    /**
     * Creates a collection {@link HashMap HashMap}, puts it in the form of keys
     * message classes, as well as listener methods for processing these messages.
     * @param controller application controller.
     */
    public RegistryHandler(Controller controller) {
        this.mapHandlers = new HashMap<>();
        try {
            locateHandlers(controller);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void locateHandlers(Controller controller) throws Exception{
        Reflections reflectionHandler = new Reflections(HANDLERS_PACKAGE);
        Reflections reflectionMessages = new Reflections(MESSAGES_PACKAGE);

        List<Class<?>> classesHandlers = new ArrayList<>(reflectionHandler.getTypesAnnotatedWith(Handler.class));
        List<Class<?>> classesMessages = new ArrayList<>(reflectionMessages.getTypesAnnotatedWith(Message.class));

        Map<String, Class<?>> mapMessages = new HashMap<>();
        for(Class <?> classMessage : classesMessages){
            mapMessages.put(classMessage.getSimpleName(), classMessage);
        }
        for(Class<?>  c: classesHandlers){
            Constructor<?> constructor = c.getConstructor(Controller.class);
            RequestHandler handler = (RequestHandler)constructor.newInstance(controller);
            Class<?> msg = mapMessages.get(c.getAnnotation(Handler.class).message());
            mapHandlers.put(msg, handler);
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
