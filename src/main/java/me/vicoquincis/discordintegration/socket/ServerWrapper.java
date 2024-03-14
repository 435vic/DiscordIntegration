package me.vicoquincis.discordintegration.socket;

import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.JettyWebSocketHandler;
import io.socket.socketio.server.SocketIoServer;
import me.vicoquincis.discordintegration.DiscordIntegration;
import org.bukkit.Bukkit;
import org.eclipse.jetty.http.pathmap.ServletPathSpec;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.EnumSet;

public class ServerWrapper {
    DiscordIntegration plugin = DiscordIntegration.getPlugin(DiscordIntegration.class);
    private final org.eclipse.jetty.server.Server mServer;
    private final EngineIoServer mEngineIoServer;
    private final SocketIoServer mSocketIoServer;

    private final HttpServlet socketIoAdapter = new HttpServlet() {
        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
            mEngineIoServer.handleRequest(request, response);
        }
    };

    public ServerWrapper(String host, int port, boolean useSSL) {
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");

        mServer = new Server();
        ServerConnector connector;
        if (useSSL) {
            plugin.getLogger().info("Using SSL for socket.io server");
            // setup ssl
            SslContextFactory.Server ssl = new SslContextFactory.Server();
            // As this project was developed privately, no robust secret storage was implemented
            // so for now the password is redacted. Replace keystore location and password with your own
            ssl.setKeyStorePath(System.getProperty("user.home") + "/.keystore/hapico/hapico.jks");
            ssl.setKeyStorePassword("redacted-password");
            ssl.setKeyManagerPassword("redacted-password");
            ssl.setKeyStoreType("JKS");
            HttpConnectionFactory http = new HttpConnectionFactory();
            SslConnectionFactory tls = new SslConnectionFactory(ssl, http.getProtocol());
            connector = new ServerConnector(mServer, tls, http);
        } else {
            connector = new ServerConnector(mServer);
        }
        connector.setPort(port);
        connector.setHost(host);
        mServer.addConnector(connector);

        mEngineIoServer = new EngineIoServer();
        mSocketIoServer = new SocketIoServer(mEngineIoServer);
        // Create the handler, and configure it to work with socket.io
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");
        handler.addFilter(RemoteAddrFilter.class, "/socket.io/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addServlet(new ServletHolder(socketIoAdapter), "/socket.io/*");

        // route all websocket stuff to engine.io
        try {
            WebSocketUpgradeFilter filter = WebSocketUpgradeFilter.configureContext(handler);
            filter.addMapping(
                new ServletPathSpec("/engine.io/*"),
                (request, response) -> new JettyWebSocketHandler(mEngineIoServer)
            );
        } catch (ServletException e) {
            e.printStackTrace();
        }
        mServer.setHandler(handler);
    }

    public void startServer() {
        try {
            mServer.start();
        } catch (Exception e) {
            Bukkit.getLogger().severe("There was an error starting the socket.io server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopServer() {
        try {
            mEngineIoServer.shutdown();
            mServer.stop();
        } catch (Exception e) {
            Bukkit.getLogger().severe("There was an error stopping the socket.io server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return mServer.isRunning();
    }

    public SocketIoServer getServer() {
        return mSocketIoServer;
    }
}
