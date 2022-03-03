package com.cloud.serverpak;

import handlers.HandlerRegistry;
import handlers.RequestHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
@Log4j2
public class MainHandler extends ChannelInboundHandlerAdapter{

    private AuthService authService;
    private static List<Channel> channels = new ArrayList<>();
    private String userName;
    private ExecutorService executorService;
    private FilesInformService filesInformService;
    private HandlerRegistry handlerRegistry;

    public MainHandler(AuthService authService) {
        this.authService = authService;
        this.executorService = Executors.newSingleThreadExecutor();
        this.filesInformService = new FilesInformService();
        this.handlerRegistry = new HandlerRegistry(this);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RequestHandler handler = handlerRegistry.getHandler(msg.getClass());
        handler.handle(ctx, msg);
    }

    public static List<Channel> getChannels() {
        return channels;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        authService.stop();
        executorService.shutdown();
        cause.printStackTrace();
        ctx.close();
        log.info("Соединение закрыто");
    }
}
