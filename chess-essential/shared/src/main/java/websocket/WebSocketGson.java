package websocket;

import com.google.gson.*;
import websocket.commands.*;
import websocket.messages.*;
import java.lang.reflect.Type;

public class WebSocketGson {
    public static Gson create() {
        Gson plain = new Gson();
        return new GsonBuilder()
                .registerTypeAdapter(UserGameCommand.class, (JsonDeserializer<UserGameCommand>) (el, type, ctx) -> {
                    String commandType = el.getAsJsonObject().get("commandType").getAsString();
                    if (UserGameCommand.CommandType.valueOf(commandType) == UserGameCommand.CommandType.MAKE_MOVE) {
                        return ctx.deserialize(el, MakeMoveCommand.class);
                    } else {
                        return plain.fromJson(el, UserGameCommand.class);
                    }
                })
                .registerTypeAdapter(ServerMessage.class, (JsonDeserializer<ServerMessage>) (el, type, ctx) -> {
                    String msgType = el.getAsJsonObject().get("serverMessageType").getAsString();
                    var messageType = ServerMessage.ServerMessageType.valueOf(msgType);
                    if (messageType == ServerMessage.ServerMessageType.LOAD_GAME) {
                        return ctx.deserialize(el, LoadGameMessage.class);
                    }
                    else if (messageType == ServerMessage.ServerMessageType.ERROR) {
                        return ctx.deserialize(el, ErrorMessage.class);
                    }
                    else if (messageType == ServerMessage.ServerMessageType.NOTIFICATION) {
                        return ctx.deserialize(el, NotificationMessage.class);
                    }
                    return null;
                })
                .create();
    }
}