package com.cloud.clientpak;

import com.cloud.clientpak.handlers.RegistryHandler;
import com.cloud.clientpak.interfaces.RequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * The main listener of the Netty pipeline, extends {@link ChannelInboundHandlerAdapter ChannelInboundHandlerAdapter},
 * the method of reading  {@link #channelRead(ChannelHandlerContext, Object) channelRead channelRead} is essentially
 * the main point connecting the Netty kernel and the program with all its actions that will be taken to process received message.
 */
@Data
@Log4j2
public class MainHandler extends ChannelInboundHandlerAdapter {

    /**
     * Username.
     */
    private String userName;

    /**
     * A listener logger for processing incoming messages.
     * @see RegistryHandler
     */
    private RegistryHandler registryHandler;

    /**
     * When created, it receives a link to the application controller.
     * @param controller Application controller.
     */
    public MainHandler(Controller controller) {
        this.registryHandler = new RegistryHandler(controller);
    }

    /**
     * Reads received message objects. Depending on the incoming class
     * messages are taken from {@link RegistryHandler HandlerRegistry} processing method
     * and stores it in the function interface variable  {@link RequestHandler RequestHandler}.
     * Calls this method for execution by passing parameters to it:
     * @param ctx channel context.
     * @param msg the message object.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        RequestHandler handler = registryHandler.getHandler(msg.getClass());
        handler.handle(ctx, msg);
    }

    /**
     * Called when errors occur, closes the channel context.
     * @param ctx channel context.
     * @param cause a variable with an exception.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.close();
        log.info("Соединение закрыто");
    }
}
