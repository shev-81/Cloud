package com.cloud.clientpak;

import config.Config;
import config.ConfigFromFile;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.*;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import messages.AbstractMessage;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

/**
 * Class describing the network connection, {@link #openConnection openConnection()}.
 * The connection must be opened in a separate thread, so as not to interfere with the operation of the application. */
@Data
@Log4j2
public class Connection implements Runnable {

    /**
     * Configuration server
     */
    private final Config config;

    /**
     * Server address.
     */
    private final String SERVER_ADDR;

    /**
     * Server port.
     */
    private final int SERVER_PORT;

    /**
     * Application controller.
     */
    private Controller controller;

    /**
     * The current connection.
     */
    private Channel currentChannel;

    /**
     * Synchronization object, used when starting a connection,
     * linked to the user's main GUI application.
     */
    private CountDownLatch countDownLatch;

    /**
     * The constructor saves a reference to the application controller
     * and the synchronization object.
     * @param controller application controller.
     * @param countDownLatch synchronization object.
     */
    public Connection(Controller controller, CountDownLatch countDownLatch) {
        config = new ConfigFromFile("./../server.properties");
        this.controller = controller;
        this.SERVER_ADDR = config.getAddress();
        this.SERVER_PORT = config.getPort();
        this.countDownLatch = countDownLatch; // Launch after all!!!
    }

    /**
     * Calls the method of opening a network connection, in a
     * separate thread.
     */
    @Override
    public void run() {
        openConnection();
    }

    /**
     * Opens a network connection. Starts the Netty client. When configuring the client
     * defines the pool of processing threads, the address and port of the server, builds a pipeline of handlers
     * incoming and outgoing data. In this case, the serializer and deserializer are used
     * objects, as well as the main listener having a link to the application controller.
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
                        protected void initChannel(SocketChannel socketChannel) {
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
            log.error(e.toString());
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                log.error(e.toString());
            }
        }
    }

    /**
     * Sends the message object through the current connection
     * channel to the Netty pipeline.
     * @param msg Message.
     * @return The current connection channel.
     */
    public ChannelFuture send(AbstractMessage msg){
        return currentChannel.writeAndFlush(msg);
    }

    /**
     * Closes the connection channel.
     */
    public void close() {
        currentChannel.close();
    }
}
