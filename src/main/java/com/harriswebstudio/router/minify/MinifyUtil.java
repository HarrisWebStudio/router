package com.harriswebstudio.router.minify;

import com.harriswebstudio.router.http.HttpStatus;
import com.harriswebstudio.router.route.RouteContext;
import net.logicsquad.minifier.Minifier;
import net.logicsquad.minifier.css.CSSMinifier;
import net.logicsquad.minifier.js.JSMinifier;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MinifyUtil {

    private static final Map<String, String> CSS_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> JS_CACHE = new ConcurrentHashMap<>();

    public static <T> T getFile(String directory, String name, T defaultValue, RouteContext context,
                                AssetFunction<InputStream, T> execute) throws Exception {
        String cacheKey = directory + "/" + name;

        if (!directory.equals("image")) {
            if (directory.equals("css") && CSS_CACHE.containsKey(cacheKey)) {
                context.setResponseType("text/css");
                return (T) CSS_CACHE.get(cacheKey);
            }

            if (directory.equals("js") && JS_CACHE.containsKey(cacheKey)) {
                context.setResponseType("application/javascript");
                return (T) JS_CACHE.get(cacheKey);
            }
        }

        Path basePath = Paths.get("src/main/resources/assets", directory).toAbsolutePath();
        Path resolvedPath = basePath.resolve(name).normalize();

        if (!resolvedPath.startsWith(basePath)) {
            context.setResponseCode(HttpStatus.FORBIDDEN);
            return defaultValue;
        }

        if (Files.exists(resolvedPath)) {
            try (InputStream fis = Files.newInputStream(resolvedPath)) {
                return execute.apply(fis);
            }
        }

        String resourcePath = "/assets/" + directory + "/" + name;
        try (InputStream inputStream = MinifyUtil.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                context.setResponseCode(HttpStatus.NOT_FOUND);
                return defaultValue;
            }

            return execute.apply(inputStream);
        }
    }

    public static String minifyCss(String name, String content) {
        try {
            if (content == null || content.isEmpty())
                return content;

            Minifier minifier = new CSSMinifier(new StringReader(content));
            StringWriter writer = new StringWriter();
            minifier.minify(writer);

            String minified = writer.toString();
            CSS_CACHE.put(name, minified);

            return minified;
        } catch (Exception exception) {
            return content;
        }
    }

    public static String minifyJs(String name, String content) {
        try {
            if (content == null || content.isEmpty())
                return content;

            Minifier minifier = new JSMinifier(new StringReader(content));
            StringWriter writer = new StringWriter();
            minifier.minify(writer);

            String minified = writer.toString();
            JS_CACHE.put(name, minified);

            return minified;
        } catch (Exception exception) {
            return content;
        }
    }

}