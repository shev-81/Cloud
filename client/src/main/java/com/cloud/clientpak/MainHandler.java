package com.cloud.clientpak;

import handlers.HandlerRegistry;
import handlers.RequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
public class MainHandler extends ChannelInboundHandlerAdapter {

    private String userName;
    private HandlerRegistry handlerRegistry;

    public MainHandler(Controller controller) {
        this.handlerRegistry = new HandlerRegistry(controller);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        RequestHandler handler = handlerRegistry.getHandler(msg.getClass());
        handler.handle(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.close();
        log.info("Соединение закрыто");
    }
}
