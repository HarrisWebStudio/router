package com.harriswebstudio.router.session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionStore {

    public static final String SESSION_COOKIE = "ROUTER_SESSION";
    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    public static Session create() {
        String id = UUID.randomUUID().toString();
        Session session = new Session(id);

        SESSIONS.put(id, session);
        return session;
    }

    public static Session get(String id) {
        return id != null ? SESSIONS.get(id) : null;
    }

    public static Session getOrCreate(String id) {
        if (id != null && SESSIONS.containsKey(id))
            return SESSIONS.get(id);

        return create();
    }

    public static void destroy(String id) {
        SESSIONS.remove(id);
    }

}