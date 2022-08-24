package com.cloud.clientpak;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.*;
import lombok.Data;
import messages.AbstractMessage;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

/**
 * Класс описывающий сетевое соединение, {@link #openConnection openConnection()}.
 * Соединение необходимо открывать в отдельном потоке, что бы не мешать работе приложения.
 */
@Data
public class Connection implements Runnable {

    /**
     * Адресс сервера.
     */
    private final String SERVER_ADDR = "localhost";

    /**
     * Порт сервера.
     */
    private final int SERVER_PORT = 8189;

    /**
     * Контроллер приложения.
     */
    private Controller controller;

    /**
     * Текущее соединение.
     */
    private Channel currentChannel;

    /**
     * Объект синхронизации, используется при запуске соединения,
     * связан с основным приложением GUI пользователя.
     */
    private CountDownLatch countDownLatch;

    /**
     * Конструктор сохраняет ссылку на контроллер приложения и объект синхронизации.
     * @param controller контроллер приложения.
     * @param countDownLatch объект синхронизации.
     */
    public Connection(Controller controller, CountDownLatch countDownLatch) {
        this.controller = controller;
        this.countDownLatch = countDownLatch;
    }

    /**
     * Вызывает метод открытия сетевого соединения, в отдельном потоке.
     */
    @Override
    public void run() {
        openConnection();
    }

    /**
     * Открывает сетевое соединение. Запускает клиент Netty. При конфигурации клиента
     * определяет пул потоков обработки, адресс и порт сервера, строит ковеер обработчиков
     * вхоядщих и исходящих данных. В данном случае используются сериализатор и десериализатор
     * объектов, а так же основной слушатель имеющий ссылку на контроллер приложения.
     * @see ObjectDecoder
     * @see ObjectEncoder
     * @see MainHandler
     * @see Bootstrap
     */
    public void openConnection() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(SERVER_ADDR, SERVER_PORT))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(1024 * 1024 * 100, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new MainHandler(controller)
                            );
                            currentChannel = socketChannel;
                        }
                    });
            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            countDownLatch.countDown();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Посылает объект сообщения через текущий канал соединения в конвеер Netty.
     * @param msg Сообщение.
     * @return Текущий канал соединения.
     */
    public ChannelFuture send(AbstractMessage msg){
        return currentChannel.writeAndFlush(msg);
    }

    /**
     * Закрывает канал соединения.
     */
    public void close() {
        currentChannel.close();
    }
}
