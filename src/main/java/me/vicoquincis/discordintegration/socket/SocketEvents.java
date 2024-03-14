package me.vicoquincis.discordintegration.socket;

import io.socket.engineio.server.Emitter;
import io.socket.socketio.server.SocketIoSocket;
import me.vicoquincis.discordintegration.DiscordIntegration;

import java.util.Arrays;

public abstract class SocketEvents {
    private static SocketIoSocket socket;

    // This class can be used to add event listeners
    public static void registerSocketListeners(SocketIoSocket sock) {
        socket = sock;
        socket.on("ping", pingPong);
    }

    private static final Emitter.Listener pingPong = new Emitter.Listener()  {
        @Override
        public void call(Object...args) {
            DiscordIntegration.logger.info("[client " + socket.getId() + "] " + "ping" + Arrays.toString(args));
            socket.send("pong");
        }
    };
}
