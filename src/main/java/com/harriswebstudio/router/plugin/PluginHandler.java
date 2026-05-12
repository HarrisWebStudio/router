package com.harriswebstudio.router.plugin;

import com.harriswebstudio.router.Router;
import com.harriswebstudio.router.plugin.annotation.PluginInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class PluginHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginHandler.class);
    private final Map<String, RouterPlugin> enabledPluginMap = new HashMap<>();
    private final Map<String, RouterPlugin> disabledPluginMap = new HashMap<>();

    private final Router router;

    public void registerPlugin(RouterPlugin plugin) {
        if (!plugin.getClass().isAnnotationPresent(PluginInfo.class)) {
            LOGGER.warn("The plugin class {} is not annotated with @PluginInfo",  plugin.getClass().getName());
            return;
        }

        PluginInfo annotation = plugin.getClass().getDeclaredAnnotation(PluginInfo.class);
        if (enabledPluginMap.containsKey(annotation.value().toLowerCase())) {
            LOGGER.warn("A plugin with the name {} is already registered", annotation.value());
            return;
        }

        long startTime = System.currentTimeMillis();

        try {
            plugin.onEnable();
            enabledPluginMap.put(annotation.value().toLowerCase(), plugin);

            LOGGER.info(
                    "The plugin {} has been registered in {}ms",
                    plugin.getClass().getName(),
                    System.currentTimeMillis() - startTime
            );
        } catch (Exception exception) {
            disabledPluginMap.put(annotation.value().toLowerCase(), plugin);
            LOGGER.error("An error occurred while trying to register the plugin {}", plugin.getClass().getName(), exception);
        }
    }

    public <T extends RouterPlugin> T getPlugin(Class<T> pluginClass) {
        String id = getId(pluginClass);
        if (id == null)
            return null;

        RouterPlugin routerPlugin = enabledPluginMap.get(id);
        if (routerPlugin != null)
            return pluginClass.cast(routerPlugin);

        routerPlugin =  disabledPluginMap.get(id);
        if (routerPlugin != null)
            return pluginClass.cast(routerPlugin);

        return null;
    }

    public List<? extends RouterPlugin> getEnabledPlugins() {
        return enabledPluginMap.values().stream().toList();
    }

    public boolean isPluginEnabled(Class<? extends RouterPlugin> pluginClass) {
        String id = getId(pluginClass);

        if (id == null)
            return false;

        return enabledPluginMap.containsKey(id);
    }

    public String getId(Class<? extends RouterPlugin> pluginClass) {
        if (!pluginClass.isAnnotationPresent(PluginInfo.class))
            return null;

        PluginInfo annotation = pluginClass.getDeclaredAnnotation(PluginInfo.class);
        return annotation.value().toLowerCase();
    }

}