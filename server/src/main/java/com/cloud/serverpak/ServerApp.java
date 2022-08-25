package com.cloud.serverpak;

import com.cloud.serverpak.interfaces.AuthService;
import com.cloud.serverpak.services.AuthServiceBD;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Класс запускающий Сервер Netty. Выполняет настройку сервера, создает
 * конвеер состоящий из {@link ObjectDecoder ObjectDecoder()},
 * {@link ObjectEncoder ObjectEncoder()} и {@link MainHandler MainHandler()}.
 * @see ServerBootstrap
 * @see NioEventLoopGroup
 */
@Data
public class ServerApp implements Runnable{

    private static final Logger LOGGER = LogManager.getLogger(ServerApp.class);

    /**
     * Сервис авторизации. {@link AuthService AuthService}
     */
    private static AuthService authService = new AuthServiceBD();

    /**
     * Текущий канал соединения.
     */
    private Channel currentChannel;

    /**
     * Пулл потоков для работы с клиентами.
     */
    private EventLoopGroup mainGroup;

    /**
     * Пулл потоков для работы с отправляемыми данными.
     */
    private EventLoopGroup workerGroup;

    /**
     * Основной слушатель входящих объектов сообщений.
     */
    private MainHandler mainHandler;

    /**
     * Метод запуска сервера. Для запуска создает 2 пула потоков, создает объект сервера,
     * Определяет для него конвеер обработки данных. В конвеере, определяет
     * максимальный размер байт для отправки (10 mb). При создании главного слушателя
     * передает ему в конструкторе ссылку на сервис авторизации.
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

    /**
     * Останавливает пулы потоков сервера и слушателя.
     */
    public void stop(){
        mainGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        AuthServiceBD.getMainHandlerList().forEach(p-> {
            p.getExecutorService().shutdown();
        });
    }
}