package com.harriswebstudio.router.plugin;

import com.harriswebstudio.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public abstract class RouterPlugin {

    protected final Router router = Router.get();
    private transient Logger logger;

    private final List<PluginModule> modules = new ArrayList<>();

    public void onEnable() throws Exception {
    }

    public void onDisable() {
    }

    public final void registerRoutes(Object... objects) {
        Router.get().registerRoutes(objects);
    }

    public final void registerListeners(Object... objects) {
        for (Object object : objects)
            Router.get().getEventHandler().registerListener(object);
    }

    public final  <T extends RouterPlugin> T withModule(PluginModule module) {
        this.modules.add(module);

        for (Object listener : module.getListeners())
            router.getEventHandler().registerListener(listener);

        module.onEnable();
        return (T) this;
    }

    public final <T extends RouterPlugin> T withModules(PluginModule... modules) {
        for (PluginModule module : modules)
            withModule(module);

        return (T) this;
    }

    public final boolean isModuleEnabled(Class<? extends PluginModule> clazz) {
        for (PluginModule module : modules) {
            if (module.getClass().equals(clazz))
                return true;
        }

        return false;
    }

    public final boolean isEnabled() {
        return Router.get().getPluginHandler().isPluginEnabled(getClass());
    }

    public final Logger getLogger() {
        if (logger == null)
            return logger = LoggerFactory.getLogger(getClass());

        return logger;
    }

    public static <T extends RouterPlugin> T getPlugin(Class<T> clazz) {
        return Router.get().getPluginHandler().getPlugin(clazz);
    }

}