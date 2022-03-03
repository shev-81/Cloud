package com.cloud.clientpak;

import handlers.HandlerRegistry;
import handlers.RequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
@Log4j2
public class MainHandler extends ChannelInboundHandlerAdapter {

    private String userName;
    private ExecutorService executorService;
    private HandlerRegistry handlerRegistry;

    public MainHandler(Controller controller) {
        this.executorService = Executors.newSingleThreadExecutor();
        this.handlerRegistry = new HandlerRegistry(controller);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        RequestHandler handler = handlerRegistry.getHandler(msg.getClass());
        handler.handle(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        executorService.shutdown();
        cause.printStackTrace();
        ctx.close();
        log.info("Соединение закрыто");
    }
}
