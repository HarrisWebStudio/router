package com.harriswebstudio.router.route.param.impl;

import com.harriswebstudio.router.route.RouteContext;
import com.harriswebstudio.router.route.annotation.parameter.QueryParam;
import com.harriswebstudio.router.route.param.ParamResolver;
import com.harriswebstudio.router.route.param.QueryDefault;
import org.eclipse.jetty.server.Request;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import static com.harriswebstudio.router.route.param.ParamRegistry.convert;

public class QueryParamResolver implements ParamResolver {

    private static final Map<Class<?>, QueryDefault> QUERY_DEFAULTS = new HashMap<>() {{
        put(int.class, new QueryDefault(int.class, 0));
        put(Integer.class, new QueryDefault(Integer.class, 0));
        put(long.class, new QueryDefault(long.class, 0L));
        put(Long.class, new QueryDefault(Long.class, 0L));
        put(double.class, new QueryDefault(double.class, 0.0));
        put(Double.class, new QueryDefault(Double.class, 0.0));
        put(boolean.class, new QueryDefault(boolean.class, false));
        put(Boolean.class, new QueryDefault(Boolean.class, false));
    }};

    @Override
    public Object resolve(Parameter param, Request request, RouteContext context) {
        QueryParam queryParam = param.getAnnotation(QueryParam.class);
        String value = context.getQueryParam(queryParam.value(), queryParam.defaultValue());

        if (value == null || value.isEmpty()) {
            if (queryParam.required())
                throw new IllegalArgumentException("Missing required query parameter: " + queryParam.value());

            return defaultFor(param.getType());
        }

        return convert(value, param.getType());
    }

    private static Object defaultFor(Class<?> type) {
        return QUERY_DEFAULTS.getOrDefault(type, new QueryDefault(type, null)).defaultValue();
    }

}