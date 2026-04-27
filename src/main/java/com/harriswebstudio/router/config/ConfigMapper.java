package com.harriswebstudio.router.config;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ConfigMapper {

    private static final Map<Class<?>, Function<Object, Object>> TYPE_CASTERS = new HashMap<>() {{
        put(Integer.TYPE, object -> ((Number) object).intValue());
        put(Integer.class, object -> ((Number) object).intValue());

        put(Long.TYPE, object -> ((Number) object).longValue());
        put(Long.class, object -> ((Number) object).longValue());

        put(Double.TYPE, object -> ((Number) object).doubleValue());
        put(Double.class, object -> ((Number) object).doubleValue());

        put(Float.TYPE, object -> ((Number) object).floatValue());
        put(Float.class, object -> ((Number) object).floatValue());

        put(Boolean.TYPE, object -> object);
        put(Boolean.class, object -> object);
    }};

    public static void populateFieldsFromMap(Map<?, ?> map, Object object) throws Exception {
        final Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String key = field.getName();

            if (!map.containsKey(key))
                continue;

            Object value = map.get(key);
            if (value == null)
                continue;

            Class<?> fieldType = field.getType();
            if (isPrimitiveOrString(value)) {
                field.set(object, castValue(value, fieldType));
            } else if (value instanceof Map<?, ?> nestedMap) {
                Object nestedObject = field.get(object);

                if (nestedObject == null) {
                    nestedObject = fieldType.getDeclaredConstructor().newInstance();
                    field.set(object, nestedObject);
                }

                populateFieldsFromMap(nestedMap, nestedObject);
            }
        }
    }

    private static Object castValue(Object value, Class<?> targetType) {
        return TYPE_CASTERS.getOrDefault(targetType, object -> object).apply(value);
    }

    public static boolean isPrimitiveOrString(Object value) {
        return value == null
                || value instanceof String
                || value instanceof Number
                || value instanceof Boolean;
    }

}
