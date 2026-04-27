package com.harriswebstudio.router.config.type;

import com.harriswebstudio.router.config.AbstractConfiguration;
import com.harriswebstudio.router.config.ConfigMapper;
import com.harriswebstudio.router.config.annotation.ConfigDescription;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class YamlConfiguration extends AbstractConfiguration {

    public YamlConfiguration(String fileName) {
        super(fileName);
    }

    @Override
    public <T extends AbstractConfiguration> T load(File directory, T config) {
        try {
            File file = new File(directory, getFileName());

            if (!file.exists()) {
                if (!directory.exists())
                    directory.mkdirs();

                if (!file.createNewFile())
                    throw new IOException("Could not create config file: " + file.getAbsolutePath());

                save(directory, config);
            }

            Yaml yaml = new Yaml();
            try (InputStream in = new FileInputStream(file)) {
                Object loaded = yaml.load(in);

                if (loaded instanceof Map<?, ?> map)
                    ConfigMapper.populateFieldsFromMap(map, config);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return config;
    }

    @Override
    public <T extends AbstractConfiguration> void save(File directory, T config) {
        try {
            File file = new File(directory, this.getFileName());
            if (!file.exists() && !file.createNewFile())
                throw new IOException("Could not create config file: " + file.getAbsolutePath());

            StringBuilder builder = new StringBuilder();
            writeObject(builder, config, 0);

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(builder.toString());
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void writeObject(StringBuilder builder, Object obj, int indent) throws IllegalAccessException {
        Class<?> clazz = obj.getClass();
        String indentStr = "  ".repeat(indent);
        List<Field> list = Arrays.stream(clazz.getDeclaredFields()).toList();

        for (Field field : list) {
            field.setAccessible(true);
            Object value = field.get(obj);
            ConfigDescription description = field.getAnnotation(ConfigDescription.class);

            if (description != null)
                builder.append((description.newLine() && list.getFirst() != field) ? "\n" : "")
                        .append(indentStr)
                        .append("# ")
                        .append(description.value())
                        .append("\n");

            String key = field.getName();
            if (ConfigMapper.isPrimitiveOrString(value)) {
                builder.append(indentStr).append(key).append(": ").append(value).append("\n");
            }else {
                builder.append(indentStr).append(key).append(":\n");
                writeObject(builder, value, indent + 1);
            }
        }
    }

}