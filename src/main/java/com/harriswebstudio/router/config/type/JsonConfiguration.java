package com.harriswebstudio.router.config.type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.harriswebstudio.router.config.AbstractConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class JsonConfiguration extends AbstractConfiguration {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public JsonConfiguration(String fileName) {
        super(fileName);
    }

    @Override
    public <T extends AbstractConfiguration> T load(File directory, T config) {
        try {
            if (!directory.exists())
                directory.mkdir();

            File file = new File(directory, getFileName());
            if (!file.exists())
                this.save(directory, config);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(file.toPath())));

            T toReturn = (T) GSON.fromJson(reader, config.getClass());
            toReturn.setFileDirectory(directory);

            return toReturn;
        } catch (Exception exception) {
            exception.printStackTrace();
            return config;
        }
    }

    @Override
    public <T extends AbstractConfiguration> void save(File directory, T config) {
        try {
            File file = new File(directory, getFileName());
            if (!file.exists())
                file.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            GSON.toJson(config, writer);

            writer.flush();
            writer.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}