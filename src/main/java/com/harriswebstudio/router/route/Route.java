package com.harriswebstudio.router.route;

import com.harriswebstudio.router.Router;
import com.harriswebstudio.router.http.HttpStatus;
import com.harriswebstudio.router.module.impl.error.RouteErrorModule;
import com.harriswebstudio.router.route.param.ParamResolver;
import com.harriswebstudio.router.route.param.ParamRegistry;
import com.harriswebstudio.router.route.response.AbstractRouteResponse;
import com.harriswebstudio.router.route.response.impl.BytesResponse;
import com.harriswebstudio.router.route.response.impl.JsonResponse;
import com.harriswebstudio.router.route.response.impl.TextResponse;
import com.harriswebstudio.router.route.response.mav.MavResponse;
import com.harriswebstudio.router.route.response.mav.ModelAndView;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class Route {

    public static final Pattern PATH_PARAM_PATTERN = Pattern.compile("\\{([^/]+)}");

    private final HttpMethod method;
    private final String path;
    private final Object handler;
    private final Method handlerMethod;
    private final RouteFunction routeFunction;

    private final Pattern pathPattern;
    private final List<String> pathParamNames;

    @Setter
    private boolean mavInitialized = false;

    public Route(HttpMethod method, String path, Object handler, Method handlerMethod) {
        this.method = method;
        this.path = path;
        this.handler = handler;
        this.handlerMethod = handlerMethod;
        this.routeFunction = null;
        this.pathParamNames = new ArrayList<>();
        this.pathPattern = compilePath(path, pathParamNames);
    }

    public Route(HttpMethod method, String path, RouteFunction routeFunction) {
        this.method = method;
        this.path = path;
        this.handler = null;
        this.handlerMethod = null;
        this.routeFunction = routeFunction;
        this.pathParamNames = new ArrayList<>();
        this.pathPattern = compilePath(path, pathParamNames);
    }

    public AbstractRouteResponse execute(Request request, RouteContext context) {
        try {
            if (routeFunction != null)
                return toResponse(routeFunction.handle(context), context);

            Parameter[] parameters = handlerMethod.getParameters();
            Object[] args = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++)
                args[i] = resolveParam(parameters[i], request, context);

            handlerMethod.setAccessible(true);

            Object result = handlerMethod.invoke(handler, args);
            return toResponse(result, context);
        } catch (Exception exception) {
            RouteErrorModule<?> errorModule = Router.get().getModule(RouteErrorModule.class);
            if (errorModule != null)
                return errorModule.handle(exception);

            Router.LOGGER.error("Error executing route: {} {}", method, path, exception);
            return new TextResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error: " + exception.getMessage());
        }
    }

    public boolean matches(String requestPath) {
        return pathPattern.matcher(requestPath).matches();
    }

    public Map<String, String> extractPathParams(String requestPath) {
        Map<String, String> params = new HashMap<>();
        Matcher matcher = pathPattern.matcher(requestPath);

        if (matcher.matches())
            for (int i = 0; i < pathParamNames.size(); i++)
                params.put(pathParamNames.get(i), matcher.group(i + 1));

        return params;
    }

    private Object resolveParam(Parameter param, Request request, RouteContext context) {
        if (param.getType().equals(RouteContext.class))
            return context;

        for (Map.Entry<Class<? extends Annotation>, ParamResolver> entry : ParamRegistry.PARAM_RESOLVERS.entrySet())
            if (param.isAnnotationPresent(entry.getKey()))
                return entry.getValue().resolve(param, request, context);

        return null;
    }

    private AbstractRouteResponse toResponse(Object result, RouteContext context) {
        if (result instanceof AbstractRouteResponse response) {
            if (response instanceof MavResponse)
                mavInitialized = true;
            if (context.getResponseType() != null)
                response.setContentType(context.getResponseType());
            if (context.getResponseCode() != null)
                response.setStatus(HttpStatus.fromCode(context.getResponseCode()));
            return response;
        }

        if (result instanceof ModelAndView mav) {
            mavInitialized = true;
            HttpStatus status = context.getResponseCode() != null ? HttpStatus.fromCode(context.getResponseCode()) : HttpStatus.OK;
            return new MavResponse(status, mav);
        }

        if (result instanceof byte[] bytes) {
            String contentType = context.getResponseType() != null ? context.getResponseType() : "application/octet-stream";
            HttpStatus status = context.getResponseCode() != null ? HttpStatus.fromCode(context.getResponseCode()) : HttpStatus.OK;
            return new BytesResponse(status, contentType, bytes);
        }

        if (result instanceof String string) {
            String contentType = context.getResponseType() != null ? context.getResponseType() : "text/plain";
            HttpStatus status = context.getResponseCode() != null ? HttpStatus.fromCode(context.getResponseCode()) : HttpStatus.OK;
            return new TextResponse(status, contentType, string);
        }

        if (result != null) {
            HttpStatus status = context.getResponseCode() != null ? HttpStatus.fromCode(context.getResponseCode()) : HttpStatus.OK;
            return new JsonResponse(status, Router.GSON.toJsonTree(result));
        }

        if (context.getResponseCode() != null)
            return new TextResponse(HttpStatus.fromCode(context.getResponseCode()), "text/plain", "");

        return new TextResponse(HttpStatus.OK, "text/plain", "");
    }

    private static Pattern compilePath(String path, List<String> paramNames) {
        StringBuilder regex = new StringBuilder();
        Matcher matcher = PATH_PARAM_PATTERN.matcher(path);
        int last = 0;

        while (matcher.find()) {
            regex.append(Pattern.quote(path.substring(last, matcher.start())));
            regex.append("([^/]+)");
            paramNames.add(matcher.group(1));
            last = matcher.end();
        }

        regex.append(Pattern.quote(path.substring(last)));
        return Pattern.compile(regex.toString());
    }

}