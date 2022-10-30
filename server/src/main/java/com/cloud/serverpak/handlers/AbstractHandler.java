package com.cloud.serverpak.handlers;


import com.cloud.serverpak.interfaces.RequestHandler;
import io.netty.channel.ChannelHandlerContext;

public abstract class AbstractHandler<T> implements RequestHandler<T> {


    public void handle(ChannelHandlerContext ctx, T msg) {
    }

    public abstract T getGeneric();
}
