package com.harriswebstudio.router;

import com.google.gson.Gson;
import com.harriswebstudio.router.config.ConfigHandler;
import com.harriswebstudio.router.module.RouterModule;
import com.harriswebstudio.router.plugin.PluginHandler;
import com.harriswebstudio.router.plugin.RouterPlugin;
import com.harriswebstudio.router.event.EventHandler;
import com.harriswebstudio.router.event.impl.RouterShutdownEvent;
import com.harriswebstudio.router.event.impl.RouterStartEvent;
import com.harriswebstudio.router.route.RouteHandler;
import com.harriswebstudio.router.server.ServerHandler;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.LinkedList;
import java.util.List;

@Getter
public class Router {

    public static final Logger LOGGER = LoggerFactory.getLogger("Router");
    public static final Gson GSON = new Gson();

    private static Router instance;
    private final QueuedThreadPool threadPool;

    private final Server server;
    private final RouterConfig config;
    private final EventHandler eventHandler;
    private final RouteHandler routeHandler;
    private final PluginHandler pluginHandler;

    private final List<RouterModule> modules = new LinkedList<>();

    @Setter
    private Runnable shutdownHook;

    public Router() {
        instance = this;
        this.config = ConfigHandler.loadConfig(new RouterConfig());
        this.threadPool = new QueuedThreadPool(
                config.getMaxThreads(),
                config.getMinThreads(),
                config.getIdleTimeout()
        );

        this.server = new Server(threadPool);
        this.eventHandler = new EventHandler();
        this.routeHandler = new RouteHandler();
        this.pluginHandler = new PluginHandler(this);

        ServerConnector connector = new ServerConnector(server);
        connector.setHost(config.getHost());
        connector.setPort(config.getPort());

        server.addConnector(connector);
        server.setHandler(new ServerHandler(this));

        Runtime.getRuntime().addShutdownHook(new Thread(
                this::shutdown, "Router - Shutdown Hook"));
    }

    private void shutdown() {
        try {
            eventHandler.callEvent(new RouterShutdownEvent());

            pluginHandler.getEnabledPluginMap().values()
                    .forEach(RouterPlugin::onDisable);

            if (shutdownHook != null)
                shutdownHook.run();

            server.stop();
        } catch (Exception exception) {
            LOGGER.error("An error occurred whilst shutting down Router", exception);
        }
    }

    public final void registerPlugins(RouterPlugin... plugins) {
        for (RouterPlugin plugin : plugins)
            pluginHandler.registerPlugin(plugin);
    }

    public final void registerRoutes(Object... objects) {
        for (Object object : objects)
            routeHandler.registerRoute(object);
    }

    public final void registerModules(RouterModule... modules) {
        for (RouterModule module : modules)
            registerModule(module);
    }

    public final void registerModule(RouterModule module) {
        if (!module.canEnable()) {
            log(Level.WARN, "Module %s did not register successfully and will be skipped.", module.getClass().getSimpleName());
            return;
        }

        modules.add(module);
    }

    @SuppressWarnings("unchecked")
    public final <T extends RouterModule> T getModule(Class<T> moduleClass) {
        return (T) modules.stream()
                .filter(moduleClass::isInstance)
                .findFirst()
                .orElse(null);
    }

    public final void log(Level level, String message, Object... args) {
        LOGGER.atLevel(level).log(String.format(message, args));
    }

    public final void start() {
        try {
            server.start();
            server.join();

            eventHandler.callEvent(new RouterStartEvent());
        } catch (Exception exception) {
            LOGGER.error("Failed to start server", exception);
        }
    }

    public static Router get() {
        return instance;
    }

}