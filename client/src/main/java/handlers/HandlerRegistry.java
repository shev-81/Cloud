package handlers;

import com.cloud.clientpak.Controller;
import lombok.Data;
import messages.*;

import java.util.HashMap;
import java.util.Map;

@Data
public class HandlerRegistry {

    private Map<Class<? extends AbstractMessage>, RequestHandler> mapHandlers;

    public HandlerRegistry(Controller controller) {
        this.mapHandlers = new HashMap<>();
        mapHandlers.put(AuthMessage.class, new AuthHandler(controller)::authHandle);
        mapHandlers.put(RegUserRequest.class, new RegUserHandler(controller)::regHandle);
        mapHandlers.put(FileMessage.class, new FileHandler(controller)::fileHandle);
        mapHandlers.put(FilesSizeRequest.class, new FilesSizeRequestHandler(controller)::filesSizeReqHandle);
    }

    public RequestHandler getHandler(Class cl) {
        return mapHandlers.get(cl);
    }
}
