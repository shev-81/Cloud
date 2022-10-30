package com.cloud.serverpak.interfaces;

import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * The functional interface used in RegistryHandler
 * to save methods and call them.
 */
@FunctionalInterface
public interface RequestHandler<T> {

    /**
     * The method is called according to the context of the passed listener method.
     * @param ctx channel context.
     * @param msg the message object.
     */
    void handle(ChannelHandlerContext ctx, T msg);

    /**
     * The default method of the interface returns an object of the message class -
     * defined in generics for a class implementing this interface.
     * @return T
     */
    default Class<T> getGenericClass() {
        try {
            Type sooper = getClass().getGenericSuperclass();
            Type t = ((ParameterizedType)sooper).getActualTypeArguments()[ 0 ];
            return (Class<T>) Class.forName( t.toString() );
        }
        catch( Exception e ) {
            return null;
        }
    }
}
