package com.harriswebstudio.router.route.param;

import com.harriswebstudio.router.Router;
import com.harriswebstudio.router.route.annotation.parameter.BodyParam;
import com.harriswebstudio.router.route.annotation.parameter.FormParam;
import com.harriswebstudio.router.route.annotation.parameter.PathParam;
import com.harriswebstudio.router.route.annotation.parameter.QueryParam;
import org.eclipse.jetty.server.Request;

import java.lang.annotation.Annotation;
import java.util.Map;

public class ParamRegistry {

    public static final Map<Class<? extends Annotation>, ParamResolver> PARAM_RESOLVERS = Map.of(
            PathParam.class, (param, request, context) -> {
                String name = param.getAnnotation(PathParam.class).value();
                return convert(context.getPathParam(name), param.getType());
            },
            QueryParam.class, (param, request, context) -> {
                QueryParam queryParam = param.getAnnotation(QueryParam.class);
                String value = context.getQueryParam(queryParam.value(), queryParam.defaultValue());

                if (queryParam.required() && (value == null || value.isEmpty()))
                    throw new IllegalArgumentException("Missing required query parameter: " + queryParam.value());

                return convert(value, param.getType());
            },
            BodyParam.class, (param, request, context) -> {
                String body = Request.as(request, String.class);
                return Router.GSON.fromJson(body, param.getType());
            },
            FormParam.class, (param, request, context) -> {
                String name = param.getAnnotation(FormParam.class).value();
                return convert(context.getFormField(name), param.getType());
            }
    );

    private static Object convert(String value, Class<?> type) {
        if (value == null)
            return null;

        if (type == String.class)
            return value;

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