package com.harriswebstudio.router;

import com.harriswebstudio.router.config.type.YamlConfiguration;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RouterConfig extends YamlConfiguration {

    private String host = "127.0.0.1";
    private int port = 80;

    private int minThreads = 8;
    private int maxThreads = 100;
    private int idleTimeout = 60000;

    public RouterConfig() {
        super("config.yml");
    }

}