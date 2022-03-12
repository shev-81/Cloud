package com.cloud.serverpak;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Data
public class ServerApp implements Runnable{
    private static final Logger LOGGER = LogManager.getLogger(ServerApp.class); // Trace < Debug < Info < Warn < Error < Fatal
    private static AuthService authService = new AuthServiceBD();
    private Channel currentChannel;
    private EventLoopGroup mainGroup;
    private EventLoopGroup workerGroup;
    private MainHandler mainHandler;

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
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(1024 * 1024 * 100, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new MainHandler(authService)
                            );
                            currentChannel = socketChannel;
                        }
                    });
            ChannelFuture future = b.bind(8189).sync();
            LOGGER.info("Сервер запущен");
            future.channel().closeFuture().sync();
        } finally {
            mainGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void stop(){
        mainGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        mainHandler.getExecutorService().shutdown();
    }
}