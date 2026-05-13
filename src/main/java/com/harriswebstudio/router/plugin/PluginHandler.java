package com.harriswebstudio.router.plugin;

import com.harriswebstudio.router.Router;
import com.harriswebstudio.router.plugin.annotation.PluginInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Getter
@RequiredArgsConstructor
public class PluginHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginHandler.class);
    private final Map<String, RouterPlugin> enabledPluginMap = new HashMap<>();
    private final Map<String, RouterPlugin> disabledPluginMap = new HashMap<>();

    private final Set<RouterPlugin> temporaryPlugins = new HashSet<>();

    private final Router router;

    public void initializeAll() {
        Map<String, RouterPlugin> candidates = new LinkedHashMap<>();
        for (RouterPlugin plugin : temporaryPlugins) {
            if (!plugin.getClass().isAnnotationPresent(PluginInfo.class)) {
                LOGGER.warn("Plugin class {} has no @PluginInfo – skipped", plugin.getClass().getName());
                continue;
            }
            String name = info(plugin).value().toLowerCase();
            if (candidates.containsKey(name)) {
                LOGGER.warn("Duplicate plugin name '{}' – second entry ignored", name);
                continue;
            }
            candidates.put(name, plugin);
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            Iterator<Map.Entry<String, RouterPlugin>> it = candidates.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, RouterPlugin> entry = it.next();
                for (String dependency : info(entry.getValue()).dependencies()) {
                    String depId = dependency.toLowerCase();

                    if (depId.isBlank())
                        continue;

                    if (!candidates.containsKey(depId) && !enabledPluginMap.containsKey(depId)) {
                        LOGGER.error("Plugin '{}' requires '{}' which is not present – it will not be loaded", info(entry.getValue()).value(), dependency);
                        disabledPluginMap.put(entry.getKey(), entry.getValue());
                        it.remove();
                        changed = true;
                        break;
                    }
                }
            }
        }

        Map<String, Set<String>> inEdges = new LinkedHashMap<>();
        Map<String, Set<String>> outEdges = new LinkedHashMap<>();

        for (String name : candidates.keySet()) {
            inEdges.put(name, new LinkedHashSet<>());
            outEdges.put(name, new LinkedHashSet<>());
        }

        for (Map.Entry<String, RouterPlugin> entry : candidates.entrySet()) {
            String name = entry.getKey();
            PluginInfo pluginInfo = info(entry.getValue());
            for (String dependency : pluginInfo.dependencies()) {
                String dependencyId = dependency.toLowerCase();

                if (!candidates.containsKey(dependencyId))
                    continue;

                inEdges.get(name).add(dependencyId);
                outEdges.get(dependencyId).add(name);
            }

            for (String soft : pluginInfo.softDependencies()) {
                String softId = soft.toLowerCase();

                if (!candidates.containsKey(softId))
                    continue;

                inEdges.get(name).add(softId);
                outEdges.get(softId).add(name);
            }
        }

        Queue<String> ready = new ArrayDeque<>();

        inEdges.forEach((key, value) -> {
            if (value.isEmpty())
                ready.add(key);
        });

        List<String> sorted = new ArrayList<>(candidates.size());
        while (!ready.isEmpty()) {
            String name = ready.poll();
            sorted.add(name);

            for (String successor : outEdges.get(name)) {
                inEdges.get(successor).remove(name);

                if (inEdges.get(successor).isEmpty())
                    ready.add(successor);
            }
        }

        if (sorted.size() != candidates.size()) {
            Set<String> cycleNodes = new LinkedHashSet<>(candidates.keySet());
            sorted.forEach(cycleNodes::remove);
            LOGGER.error("Circular dependency detected among plugins {} – they will not be loaded", cycleNodes);
            cycleNodes.forEach(n -> disabledPluginMap.put(n, candidates.get(n)));
        }

        for (String name : sorted)
            enablePlugin(candidates.get(name));

        temporaryPlugins.clear();
    }

    public void registerPlugin(RouterPlugin plugin) {
        if (!plugin.getClass().isAnnotationPresent(PluginInfo.class)) {
            LOGGER.warn("The plugin class {} is not annotated with @PluginInfo", plugin.getClass().getName());
            return;
        }

        temporaryPlugins.add(plugin);
    }

    private void enablePlugin(RouterPlugin plugin) {
        PluginInfo annotation = info(plugin);
        String id = annotation.value().toLowerCase();
        if (enabledPluginMap.containsKey(id)) {
            LOGGER.warn("A plugin with the name '{}' is already registered", annotation.value());
            return;
        }

        long startTime = System.currentTimeMillis();
        try {
            plugin.onEnable();
            enabledPluginMap.put(id, plugin);
            LOGGER.info("The plugin {} has been registered in {}ms",
                    plugin.getClass().getName(), System.currentTimeMillis() - startTime);
        } catch (Exception exception) {
            disabledPluginMap.put(id, plugin);
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

        routerPlugin = disabledPluginMap.get(id);
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

    public boolean isPluginEnabled(String name) {
        return enabledPluginMap.containsKey(name.toLowerCase());
    }

    public String getId(Class<? extends RouterPlugin> pluginClass) {
        if (!pluginClass.isAnnotationPresent(PluginInfo.class))
            return null;

        PluginInfo annotation = pluginClass.getDeclaredAnnotation(PluginInfo.class);
        return annotation.value().toLowerCase();
    }

    private PluginInfo info(RouterPlugin plugin) {
        return plugin.getClass().getDeclaredAnnotation(PluginInfo.class);
    }


}

