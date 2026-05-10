package com.harriswebstudio.router.plugin;

import com.harriswebstudio.router.Router;

public abstract class RouterPlugin {

    public void onEnable(Router router) throws Exception {
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

    public boolean isEnabled() {
        return Router.get().getPluginHandler().isPluginEnabled(getClass());
    }

}