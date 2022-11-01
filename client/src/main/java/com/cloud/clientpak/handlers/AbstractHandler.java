package com.cloud.clientpak.handlers;


import com.cloud.clientpak.interfaces.RequestHandler;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.ParameterizedType;

public abstract class AbstractHandler<T> implements RequestHandler<T> {


    public void handle(ChannelHandlerContext ctx, T msg) {
    }

    public Class<?> getGeneric(){
        Class<?> kl = this.getClass();
        return (Class<?>)((ParameterizedType)kl.getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
