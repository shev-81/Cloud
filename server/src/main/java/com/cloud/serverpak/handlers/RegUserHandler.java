package com.cloud.serverpak.handlers;

import com.cloud.serverpak.interfaces.AuthService;
import com.cloud.serverpak.MainHandler;
import io.netty.channel.ChannelHandlerContext;
import messages.RegUserRequest;

/**
 * Message Listener class {@link RegUserRequest RegUserRequest}.
 */
@Handler
public class RegUserHandler extends AbstractHandler<RegUserRequest> {

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
    @Override
    public void handle(ChannelHandlerContext ctx, RegUserRequest msg) {
        if (msg.getNameUser().equals(authService.getNickByLoginPass(msg.getLogin(), msg.getPassUser()))) {
            ctx.writeAndFlush(new RegUserRequest("none", "", ""));
        } else {
            if (authService.registerNewUser(msg.getNameUser(), msg.getLogin(), msg.getPassUser())) {
                ctx.writeAndFlush(new RegUserRequest("reg", "", ""));
            }
        }
    }

    @Override
    public RegUserRequest getGeneric() {
        return new RegUserRequest();
    }

}
