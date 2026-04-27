package com.harriswebstudio.router.session;

import java.util.HashMap;
import java.util.Map;

public class Session {

    private final String id;
    private final Map<String, Object> attributes = new HashMap<>();

    public Session(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void set(String key, Object value) {
        attributes.put(key, value);
    }

    public Object get(String key) {
        return attributes.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        return (T) attributes.get(key);
    }

    public boolean has(String key) {
        return attributes.containsKey(key);
    }

    public void remove(String key) {
        attributes.remove(key);
    }

    public void clear() {
        attributes.clear();
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

}

