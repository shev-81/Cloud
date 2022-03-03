package handlers;

import com.cloud.serverpak.AuthService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelHandlerContext;
import messages.RegUserRequest;

public class RegUserHandler{

    private AuthService authService;

    public RegUserHandler(MainHandler mainHandler) {
        this.authService = mainHandler.getAuthService();
    }

    public void regHandle(ChannelHandlerContext ctx, Object msg) {
        RegUserRequest regMsg = (RegUserRequest) msg;
        if (regMsg.getNameUser().equals(authService.getNickByLoginPass(regMsg.getLogin(), regMsg.getPassUser()))) {
            ctx.writeAndFlush(new RegUserRequest("none", "", ""));
        } else {
            if (authService.registerNewUser(regMsg.getNameUser(), regMsg.getLogin(), regMsg.getPassUser())) {
                ctx.writeAndFlush(new RegUserRequest("reg", "", ""));
            }
        }
    }
}
