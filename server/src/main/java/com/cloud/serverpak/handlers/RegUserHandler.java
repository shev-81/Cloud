package com.cloud.serverpak.handlers;

import com.cloud.serverpak.interfaces.AuthService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelHandlerContext;
import messages.RegUserRequest;

/**
 * Message Listener class {@link RegUserRequest RegUserRequest}.
 */
public class RegUserHandler{

    /**
     * Authorization service.
     */
    private final AuthService authService;

    /**
     * The constructor saves a reference to the main listener.
     * @param mainHandler Netty's main listener.
     */
    public RegUserHandler(MainHandler mainHandler) {
        this.authService = mainHandler.getAuthService();
    }

    /**
     * Processes a service message - a request to register a new user,
     * checks if there is a user with the same nickname in the database, if there is, then sends
     * refusal to register, and in the absence registers it and sends a response to the client,
     * that the registration was successful.
     * @param ctx channel context.
     * @param msg the message object.
     */
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
