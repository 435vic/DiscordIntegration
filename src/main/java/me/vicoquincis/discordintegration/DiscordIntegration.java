package me.vicoquincis.discordintegration;

import io.socket.socketio.server.SocketIoSocket;
import me.vicoquincis.discordintegration.api.API;
import me.vicoquincis.discordintegration.command.*;
import me.vicoquincis.discordintegration.socket.ServerWrapper;
import me.vicoquincis.discordintegration.socket.SocketEvents;
import me.vicoquincis.discordintegration.util.DiscordUserProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.restlet.data.Protocol;

import java.util.logging.Logger;

public final class DiscordIntegration extends JavaPlugin {
    public static API api;
    public static Logger logger;

    private ServerWrapper serverWrapper;
    private DiscordUserProvider userProvider;
    private SocketIoSocket socket;

    @Override
    public void onEnable() {
        logger = this.getLogger();
        api = new API(Protocol.HTTPS, 8182, "/api");
        serverWrapper = new ServerWrapper("0.0.0.0", 25629, true);
        userProvider = new DiscordUserProvider();

        this.getServer().getPluginManager().registerEvents(new Events(), this);
        this.getCommand("msg").setExecutor(new MessageCommand());
        this.getCommand("msg").setTabCompleter(new MessageCommandCompleter());
        this.getCommand("mdi").setExecutor(new DICommand());
        this.getCommand("mdi").setTabCompleter(DICommand.completer);

        logger.info("Attempting to start socket.io server...");
        serverWrapper.startServer();
        logger.info("Server started.");

        serverWrapper.getServer().namespace("/").on("connection", args -> {
            SocketIoSocket sock = (SocketIoSocket) args[0];
            if (socket != null) {
                logger.info("Socket already in use by " + getSocketName(socket) + ", disconnecting new user " + getSocketName(sock));
                sock.disconnect(false);
                sock.disconnect(true);
                return;
            }
            socket = sock;
            logger.info("received connection from " + getSocketName(socket));
            Bukkit.getScheduler().runTaskLater(this, bukkitTask -> {
                logger.info("requesting discord user list...");
                userProvider.requestList();
            }, 30);
            socket.on("disconnect", data -> {
                logger.info("client " + socket.getId() + " has disconnected (" + data[0] + ")");
                socket = null;
            });
            // register event listeners (data from discord to minecraft)
            SocketEvents.registerSocketListeners(socket);
        });
    }

    @Override
    public void onDisable() {
        api.stop();
        logger.info("Attempting to stop socket.io server...");
        serverWrapper.stopServer();
        logger.info("Stopped.");
    }

    public ServerWrapper getSocketServer() {
        return serverWrapper;
    }

    public SocketIoSocket getSocket() {
        return socket;
    }

    public boolean isSocketConnected() {
        return socket != null;
    }

    public String getSocketName(SocketIoSocket sock) {
        return socket.getId() + "(" + socket.getInitialHeaders().get("remote_addr") + ")";
    }

    public DiscordUserProvider getUserProvider() {
        return userProvider;
    }

    public void socketSend(String event, Object... args) {
        try {
            getSocket().send(event, args);
        } catch (NullPointerException e) {
            getLogger().warning("client is not connected yet, event \"" + event + "\" not sent");
        }
    }

    public void socketSend(String event, Object[] args, SocketIoSocket.ReceivedByRemoteAcknowledgementCallback callback) {
        try {
            getSocket().send(event, args, callback);
        } catch (NullPointerException e) {
            getLogger().warning("client is not connected yet, event \"" + event + "\" not sent");
        }
    }
}
