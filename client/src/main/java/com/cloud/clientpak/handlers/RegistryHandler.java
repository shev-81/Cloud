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
    /**
     * A variable with a listener map.
     */
    private Map< Class<? extends AbstractMessage>, RequestHandler> mapHandlers;

    //todo добавить сервис локатор, через рефлексию сканирования пакета на наличие хендлеров.

    /**
     * Creates a collection {@link HashMap HashMap}, puts it in the form of keys
     * message classes, as well as listener methods for processing these messages.
     * @param controller application controller.
     */
    public RegistryHandler(Controller controller) {
        //todo перевести на дженерики и функциональный интерфейс переделать на дженерики
        this.mapHandlers = new HashMap<>();
        try {
            locateHandlers(controller);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void locateHandlers(Controller controller) throws Exception{
        Reflections reflections = new Reflections(HANDLERS_PACKAGE);
        List<Class<?>> classes = new ArrayList<>(reflections.getTypesAnnotatedWith(Handler.class));
        for(Class<?>  c: classes){
            Constructor<?> constructor = c.getConstructor(Controller.class);
            RequestHandler handler = (RequestHandler)constructor.newInstance(controller);

            //todo разобраться с рефлексией и созданием локатора хенлеров


            Class handlerMessageClass = handler.getClass();
            mapHandlers.put(handlerMessageClass, handler);
        }
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
