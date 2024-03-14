package me.vicoquincis.discordintegration.api;

import me.vicoquincis.discordintegration.api.resource.*;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class RESTApplication extends Application {
    public RESTApplication() {
        // Entirely disable ALL restlet logging cuz it interferes with all other logs
        Logger logger = LogManager.getLogManager().getLogger("");
        Handler[] handlers = logger.getHandlers();
        logger.removeHandler(handlers[0]);
        SLF4JBridgeHandler.install();
    }

    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());
//        router.attach("/test/{{uid}}", MainResource.class);
        router.attach("/players", PlayerList.class);
        router.attach("/players/uname/{{uname}}", PlayerResource.class);
        router.attach("/players/uname/{{uname}}/message", PrivateMessage.class);
        router.attach("/players/whitelist", Whitelist.class);
        router.attach("/players/whitelist/{{uname}}", WhitelistResource.class);
        router.attach("/players/bans", BanList.class);
        router.attach("/players/bans/{{uname}}", BanResource.class);
        router.attach("/chat", ChatMessage.class);
        return router;
    }
}
