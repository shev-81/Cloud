package handlers;

import com.cloud.serverpak.AuthService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelHandlerContext;

public class FileHandler  implements RequestHandler{

    private MainHandler mainHandler;
    private AuthService authService;

    public FileHandler(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
        this.authService = mainHandler.getAuthService();
    }

    @Override
    public void handle(ChannelHandlerContext ctx, Object msg) {



    }
}
