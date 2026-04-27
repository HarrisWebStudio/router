package com.harriswebstudio.router;

import com.harriswebstudio.router.config.annotation.ConfigDescription;
import com.harriswebstudio.router.config.type.YamlConfiguration;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RouterConfig extends YamlConfiguration {

    @ConfigDescription("What the server binds to.")
    private String host = "127.0.0.1";
    private int port = 80;

    @ConfigDescription("Thread pool settings, adjust for your server and load.")
    private int minThreads = 8;
    private int maxThreads = 100;
    private int idleTimeout = 60000;

    public RouterConfig() {
        super("router.yml");
    }

}