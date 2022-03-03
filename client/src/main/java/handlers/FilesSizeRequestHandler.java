package handlers;

import com.cloud.clientpak.MainHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FilesListRequestHandler{

    private MainHandler mainHandler;

    public FilesListRequestHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
    }

    public void filesListHandle(ChannelHandlerContext ctx, Object msg) {


    }
}
