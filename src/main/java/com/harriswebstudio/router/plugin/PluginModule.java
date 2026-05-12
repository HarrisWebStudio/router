package com.harriswebstudio.router.plugin;

import java.util.ArrayList;
import java.util.List;

public interface PluginModule {

    default void onEnable() {
    }

    default void onDisable() {
    }

    default List<Object> getListeners() {
        return new ArrayList<>();
    }

}