package handlers;

import io.netty.channel.ChannelHandlerContext;

public interface RequestHandler {
    void handle(ChannelHandlerContext ctx, Object msg);
}
