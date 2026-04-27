package com.harriswebstudio.router.route.annotation;

import com.harriswebstudio.router.route.annotation.method.*;
import org.eclipse.jetty.http.HttpMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

public class AnnotationRegistry {

    private static final Map<Class<? extends Annotation>, HttpMethod> METHOD_ANNOTATIONS = Map.of(
            GET.class, HttpMethod.GET,
            POST.class, HttpMethod.POST,
            PUT.class, HttpMethod.PUT,
            DELETE.class, HttpMethod.DELETE,
            PATCH.class, HttpMethod.PATCH
    );

    private static final Map<Class<? extends Annotation>, Function<Annotation, String>> PATH_EXTRACTORS = Map.of(
            GET.class, annotation -> ((GET) annotation).value(),
            POST.class, annotation -> ((POST) annotation).value(),
            PUT.class, annotation -> ((PUT) annotation).value(),
            DELETE.class, annotation -> ((DELETE) annotation).value(),
            PATCH.class, annotation -> ((PATCH) annotation).value()
    );

    public static HttpMethod getHttpMethod(Method method) {
        for (Map.Entry<Class<? extends Annotation>, HttpMethod> entry : METHOD_ANNOTATIONS.entrySet()) {
            if (method.isAnnotationPresent(entry.getKey()))
                return entry.getValue();
        }

        return null;
    }

    public static String getPath(Method method) {
        Path path = method.getAnnotation(Path.class);
        if (path != null)
            return path.value();

        for (var entry : PATH_EXTRACTORS.entrySet()) {
            Annotation annotation = method.getAnnotation(entry.getKey());

            if (annotation != null)
                return entry.getValue().apply(annotation);
        }

        return null;
    }

}