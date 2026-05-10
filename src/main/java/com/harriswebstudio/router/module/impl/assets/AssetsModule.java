package com.harriswebstudio.router.module.impl.assets;

import com.harriswebstudio.router.module.RouterModule;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class AssetsModule implements RouterModule {

    private final String assetsUrlPrefix;
    private final Map<String, byte[]> cache = new ConcurrentHashMap<>();

    public boolean handle(Request request, Response response, Callback callback) {
        String path = request.getHttpURI().getPath();
        if (!path.startsWith(assetsUrlPrefix))
            return false;

        String assetPath = path.substring(assetsUrlPrefix.length());
        if (assetPath.startsWith("/"))
            assetPath = assetPath.substring(1);

        if (assetPath.isEmpty()) {
            response.setStatus(404);
            response.write(true, ByteBuffer.allocate(0), callback);
            return true;
        }

        try {
            String[] parts = assetPath.split("/", 2);
            String directory = parts.length > 1 ? parts[0] : "";
            String name = parts.length > 1 ? parts[1] : parts[0];

            String contentType = detectContentType(name);

            if (directory.equals("css") || directory.equals("js")) {
                String cacheKey = directory + "/" + name;

                byte[] cached = cache.get(cacheKey);
                if (cached != null) {
                    response.setStatus(200);
                    response.getHeaders().put("Content-Type", contentType);
                    response.write(true, ByteBuffer.wrap(cached), callback);
                    return true;
                }

                byte[] data = readAsset(directory, name);
                if (data == null) {
                    response.setStatus(404);
                    response.write(true, ByteBuffer.allocate(0), callback);
                    return true;
                }

                String content = new String(data, StandardCharsets.UTF_8);
                String minified = directory.equals("css")
                        ? MinifyUtil.minifyCss(cacheKey, content)
                        : MinifyUtil.minifyJs(cacheKey, content);

                byte[] minifiedBytes = minified.getBytes(StandardCharsets.UTF_8);
                cache.put(cacheKey, minifiedBytes);

                response.setStatus(200);
                response.getHeaders().put("Content-Type", contentType);
                response.write(true, ByteBuffer.wrap(minifiedBytes), callback);
                return true;
            }

            byte[] data = readAsset(directory.isEmpty() ? null : directory, name);
            if (data == null) {
                response.setStatus(404);
                response.write(true, ByteBuffer.allocate(0), callback);
                return true;
            }

            response.setStatus(200);
            response.getHeaders().put("Content-Type", contentType);
            response.write(true, ByteBuffer.wrap(data), callback);
            return true;

        } catch (Exception e) {
            response.setStatus(500);
            response.write(true, ByteBuffer.allocate(0), callback);
            return true;
        }
    }

    private byte[] readAsset(String directory, String name) throws Exception {
        String resourcePath = directory != null
                ? "/assets/" + directory + "/" + name
                : "/assets/" + name;

        Path basePath = Paths.get("src/main/resources/assets",
                directory != null ? directory : "").toAbsolutePath();
        Path resolved = basePath.resolve(name).normalize();

        if (!resolved.startsWith(basePath))
            return null;

        if (Files.exists(resolved))
            return Files.readAllBytes(resolved);

        try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
            if (stream == null)
                return null;

            return stream.readAllBytes();
        }
    }

    private String detectContentType(String name) {
        String lower = name.toLowerCase();
        AssetExtension extension = AssetExtension.fromString(lower);

        if (extension != null)
            return extension.getContentType();

        return "application/octet-stream";
    }

}