package com.cloud.serverpak;

import com.cloud.serverpak.interfaces.AuthService;
import com.cloud.serverpak.services.AuthServiceBD;
import config.Config;
import config.ConfigFromFile;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * The class that starts the Netty Server. Performs server configuration, creates
 * pipeline consisting of {@link ObjectDecoder ObjectDecoder()},
 * {@link ObjectEncoder ObjectEncoder()} and {@link MainHandler MainHandler()}.
 * @see ServerBootstrap
 * @see NioEventLoopGroup
 */
@Data
@Log4j2
public class ServerApp implements Runnable{

    /**
     * Authorization service. {@link AuthService AuthService}
     */
    private static AuthService authService = new AuthServiceBD();

    private static Config config = new ConfigFromFile("./../server.properties");

    /**
     * The current connection channel.
     */
    private Channel currentChannel;

    /**
     * A thread pool for working with clients.
     */
    private EventLoopGroup mainGroup;

    /**
     * A pool of threads for working with the data being sent.
     */
    private EventLoopGroup workerGroup;

    /**
     * The method of starting the server. To run, creates 2 thread pools, creates a server object,
     * Defines a data processing pipeline for it. In the conveyor, defines
     * maximum size of bytes to send (10 mb). When creating the main listener
     * passes him a link to the authorization service in the constructor.
     */
    @SneakyThrows
    @Override
    public void run(){
        mainGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(mainGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(1024 * 1024 * 100, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new MainHandler(authService, config)
                            );
                            currentChannel = socketChannel;
                        }
                    });
            ChannelFuture future = b.bind(config.getPort()).sync();
            log.info("Сервер запущен");
            future.channel().closeFuture().sync();
        } finally {
            mainGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info("Server is stopped.");
        }
    }

    /**
     * Stops the server and listener thread pools.
     */
    public void stop(){
        mainGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        AuthServiceBD.getMainHandlerList()
                .forEach(p-> p.getExecutorService()
                        .shutdown());
    }
}