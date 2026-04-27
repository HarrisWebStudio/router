package com.harriswebstudio.router.module;

public interface RouterModule {

    default boolean canEnable() {
        return true;
    }

}