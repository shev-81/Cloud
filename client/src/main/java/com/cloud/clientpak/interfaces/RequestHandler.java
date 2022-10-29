package com.cloud.clientpak.interfaces;

import com.cloud.clientpak.handlers.RegistryHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * The functional interface used in {@link RegistryHandler HandlerRegistry}
 * to save methods and call them.
 */
@FunctionalInterface
public interface RequestHandler{

    /**
     * The method is called according to the context of the passed listener method.
     * @param ctx channel context.
     * @param msg the message object.
     */
    void handle(ChannelHandlerContext ctx, Object msg);
}
