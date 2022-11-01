package com.cloud.serverpak;

import com.cloud.serverpak.handlers.RegistryHandler;
import com.cloud.serverpak.interfaces.RequestHandler;
import com.cloud.serverpak.interfaces.AuthService;
import com.cloud.serverpak.services.AuthServiceBD;
import com.cloud.serverpak.services.FilesInformService;
import com.cloud.serverpak.services.ServiceLocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The main listener of the Netty pipeline, extends {@link ChannelInboundHandlerAdapter ChannelInboundHandlerAdapter},
 * the method of reading  {@link #channelRead(ChannelHandlerContext, Object) channelRead channelRead} is essentially
 * the main point connecting the Netty kernel and the program with all its actions that will be taken to process received message.
 */
@Data
@Log4j2
public class MainHandler extends ChannelInboundHandlerAdapter{

    /**
     * Authorization service {@link AuthService AuthService}.
     */
    private AuthService authService;
    private static List<Channel> channels = new ArrayList<>();
    private String userName;
    private ExecutorService executorService;
    private FilesInformService filesInformService;
    private ServiceLocator serviceLocator;

    /**
     * A listener logger for processing incoming messages.
     * @see RegistryHandler
     */
    private RegistryHandler handlerRegistry;

    /**
     * Receives a link to the authorization service when creating it. Launches
     * thread pool, file management service, and message listener logger.
     * @param authService Authorization service.
     * @see Executors
     * @see FilesInformService
     * @see RegistryHandler
     */
    public MainHandler(AuthService authService, ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
        this.authService = authService;
        this.executorService = Executors.newSingleThreadExecutor();
        this.filesInformService = new FilesInformService();
        this.handlerRegistry = new RegistryHandler(this);
        AuthServiceBD.getMainHandlerList().add(this);
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
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RequestHandler handler = handlerRegistry.getHandler(msg.getClass());
        handler.handle(ctx, msg);
    }

    /**
     * Called when errors occur, closes the channel context.
     * @param ctx channel context.
     * @param cause a variable with an exception.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        authService.stop();
        executorService.shutdown();
        cause.printStackTrace();
        ctx.close();
        log.info("Соединение закрыто");
    }

    public List<Channel> getChannels() {
        return channels;
    }
}
