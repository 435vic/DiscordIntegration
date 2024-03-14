package me.vicoquincis.discordintegration.api;

import me.vicoquincis.discordintegration.DiscordIntegration;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.ssl.DefaultSslContextFactory;
import org.restlet.service.LogService;
import org.restlet.util.Series;

import java.util.logging.Level;

public class API {
    private Component component;

    public API(Protocol protocol, int port, String root) {
        component = new Component();
        if (protocol == Protocol.HTTP) {
            component.getServers().add(protocol, port);
            component.getDefaultHost().attach(root, new RESTApplication());
            try {
                DiscordIntegration.logger.info("Starting Restlet server...");
                component.start();
            } catch (Exception e) {
                DiscordIntegration.logger.log(Level.SEVERE, "There was an error starting the server: " + e.toString());
            }
        } else {
            component = new Component();
            component.setLogService(new LogService(false));
            Server server = component.getServers().add(Protocol.HTTPS, 8183);
            Series<Parameter> parameters = server.getContext().getParameters();
            DefaultSslContextFactory sslContextFactory = new DefaultSslContextFactory();
            // As this project was developed privately, no robust secret storage was implemented
            // so for now the password is redacted. Replace keystore location and password with your own
            sslContextFactory.setProtocol("SSL");
            sslContextFactory.setKeyStorePath(System.getProperty("user.home") + "/.keystore/hapico/hapico.jks");
            sslContextFactory.setKeyStorePassword("redacted-password");
            sslContextFactory.setKeyStoreKeyPassword("redacted-password");
            sslContextFactory.setKeyStoreType("JKS");
            server.getContext().getAttributes().put("sslContextFactory", sslContextFactory);
            component.getDefaultHost().attach(root, new RESTApplication());
            try {
                DiscordIntegration.logger.info("Starting HTTPS Restlet server...");
                component.start();
            } catch (Exception e) {
                DiscordIntegration.logger.log(Level.SEVERE, "There was an error starting the server: " + e.toString());
            }
        }
    }

    public boolean isRunning() {
        return component.isStarted();
    }

    public void stop() {
        try {
            component.stop();
        } catch (Error | Exception e) {
            DiscordIntegration.logger.log(Level.SEVERE, "There was an error stopping the server: " + e.toString());
            // try one more time for good measure
            try {
                component.stop();
            } catch (Exception ignored) {}
        }
    }

    public void start() {
        try {
            component.start();
        } catch (Exception e) {
            DiscordIntegration.logger.log(Level.SEVERE, "There was an error starting the server: " + e.toString());
        }
    }
}
