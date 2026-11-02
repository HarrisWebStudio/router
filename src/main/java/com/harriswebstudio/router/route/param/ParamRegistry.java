package com.harriswebstudio.router.route.param;

import com.harriswebstudio.router.route.annotation.parameter.BodyParam;
import com.harriswebstudio.router.route.annotation.parameter.FormParam;
import com.harriswebstudio.router.route.annotation.parameter.PathParam;
import com.harriswebstudio.router.route.annotation.parameter.QueryParam;
import com.harriswebstudio.router.route.param.impl.BodyParamResolver;
import com.harriswebstudio.router.route.param.impl.FormParamResolver;
import com.harriswebstudio.router.route.param.impl.PathParamResolver;
import com.harriswebstudio.router.route.param.impl.QueryParamResolver;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.UUID;

public class ParamRegistry {

    public static final Map<Class<? extends Annotation>, ParamResolver> PARAM_RESOLVERS = Map.of(
            PathParam.class, (param, request, context) -> new PathParamResolver(),
            QueryParam.class, (param, request, context) -> new QueryParamResolver(),
            BodyParam.class, (param, request, context) -> new BodyParamResolver(),
            FormParam.class, (param, request, context) -> new FormParamResolver()
    );

    public static Object convert(String value, Class<?> type) {
        if (value == null)
            return null;

        if (type == String.class)
            return value;

        if (type == UUID.class)
            return UUID.fromString(value);

        if (type == int.class || type == Integer.class)
            return Integer.parseInt(value);

        if (type == long.class || type == Long.class)
            return Long.parseLong(value);

        if (type == boolean.class || type == Boolean.class)
            return Boolean.parseBoolean(value);

        if (type == double.class || type == Double.class)
            return Double.parseDouble(value);

        return value;
    }

}