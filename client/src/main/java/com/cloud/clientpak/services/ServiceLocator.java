package com.cloud.clientpak.services;

import com.cloud.clientpak.Controller;
import com.cloud.clientpak.handlers.Handler;
import config.Config;
import lombok.Data;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

@Data
public class ServiceLocator {

    private Config config;

    private List<Constructor<?>> listConstructors;

    public ServiceLocator(Config config){
        this.config = config;
        try {
            this.listConstructors = locateServices();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private List<Constructor<?>> locateServices() throws Exception{
        Reflections reflectionHandler = new Reflections(config.getPackageHandlers());
        List<Class<?>> classesHandlers = new ArrayList<>(reflectionHandler.getTypesAnnotatedWith(Handler.class));
        List<Constructor<?>> list = new ArrayList<>();
        for(Class<?>  c: classesHandlers){
            Constructor<?> constructor = c.getConstructor(Controller.class);
            list.add(constructor);
        }
        return list;
    }
}
