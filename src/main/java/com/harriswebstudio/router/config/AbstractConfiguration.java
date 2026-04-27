package com.harriswebstudio.router.config;

import lombok.Data;

import java.io.File;

@Data
public abstract class AbstractConfiguration {

    private final transient String fileName;
    private transient File fileDirectory;

    public abstract <T extends AbstractConfiguration> T load(File directory, final T config);

    public abstract <T extends AbstractConfiguration> void save(File directory, final T config);

    public final void save() {
        save(getFileDirectory(), this);
    }

}