package com.harriswebstudio.router.route;

import com.harriswebstudio.router.Router;
import com.harriswebstudio.router.event.impl.route.RouteRegisterEvent;
import com.harriswebstudio.router.mav.AbstractMav;
import com.harriswebstudio.router.mav.thymeleaf.ThymeleafMav;
import com.harriswebstudio.router.route.annotation.AnnotationRegistry;
import com.harriswebstudio.router.route.annotation.Path;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RouteHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteHandler.class);
    private final List<Route> routes = new ArrayList<>();

    private AbstractMav mavImplementation = new ThymeleafMav();
    private boolean mavErrors = true;

    public void registerRoute(Object object) {
        String basePath = "";
        if (object.getClass().isAnnotationPresent(Path.class)) {
            String pathValue = object.getClass().getAnnotation(Path.class).value();

            if (pathValue != null)
                basePath = pathValue;
        }

        for (Method method : object.getClass().getDeclaredMethods()) {
            HttpMethod httpMethod = AnnotationRegistry.getHttpMethod(method);
            if (httpMethod == null)
                continue;

            String methodPath = AnnotationRegistry.getPath(method);
            if ((methodPath == null || methodPath.isEmpty()) && basePath.isEmpty()) {
                LOGGER.warn("Method {} has no path, skipping", method.getName());
                continue;
            }

            if (methodPath == null)
                methodPath = "";

            String fullPath = (basePath + methodPath).replaceAll("/+", "/");
            if (!fullPath.startsWith("/"))
                fullPath = "/" + fullPath;

            Route route = new Route(httpMethod, fullPath, object, method);
            initializeRoute(route, httpMethod, fullPath);
        }
    }

    public void registerRoute(HttpMethod httpMethod, String path, RouteFunction function) {
        String fullPath = path.replaceAll("/+", "/");
        if (!fullPath.startsWith("/"))
            fullPath = "/" + fullPath;

        Route route = new Route(httpMethod, fullPath, function);
        initializeRoute(route, httpMethod, fullPath);
    }

    public void initializeRoute(Route route, HttpMethod httpMethod, String fullPath) {
        routes.add(route);

        Router.get().getEventHandler().callEvent(
                new RouteRegisterEvent(route));

        LOGGER.info("Registered route {} {}", httpMethod, fullPath);
    }

    public Route getRoute(HttpMethod method, String path) {
        return routes.stream()
                .filter(route -> route.getMethod().equals(method) && route.matches(path))
                .findFirst()
                .orElse(null);
    }

    public boolean isMavInitialized() {
        return routes.stream().anyMatch(Route::isMavInitialized);
    }

}