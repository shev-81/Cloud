package com.cloud.clientpak;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.*;
import lombok.Data;
import messages.AbstractMessage;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

@Data
public class Connection implements Runnable {

    private final String SERVER_ADDR = "192.168.1.205";  //192.168.1.205";
    private final int SERVER_PORT = 8189;
    private Controller controller;
    private Channel currentChannel;
    private CountDownLatch countDownLatch;

    public Connection(Controller controller, CountDownLatch countDownLatch) {
        this.controller = controller;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        openConnection();
    }

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

    public void send(AbstractMessage msg){
        currentChannel.writeAndFlush(msg);
    }

    public void close() {
        currentChannel.close();
    }
}
