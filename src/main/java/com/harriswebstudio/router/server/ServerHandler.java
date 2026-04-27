package com.harriswebstudio.router.server;

import com.harriswebstudio.router.Router;
import com.harriswebstudio.router.module.impl.RouteAfterModule;
import com.harriswebstudio.router.module.impl.RouteBeforeModule;
import com.harriswebstudio.router.route.Route;
import com.harriswebstudio.router.route.RouteContext;
import com.harriswebstudio.router.route.response.AbstractRouteResponse;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MultiPartConfig;
import org.eclipse.jetty.http.MultiPartFormData;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RequiredArgsConstructor
public class ServerHandler extends Handler.Abstract {

    private final Router router;

    @Override
    public boolean handle(Request request, Response response, Callback callback) {
        String path = request.getHttpURI().getPath();
        Route route = router.getRouteHandler().getRoute(
                HttpMethod.fromString(request.getMethod()), path);

        if (route == null) {
            response.setStatus(404);
            response.write(true, ByteBuffer.wrap("Not Found".getBytes(StandardCharsets.UTF_8)), callback);
            return true;
        }

        String contentType = request.getHeaders().get("Content-Type");
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            MultiPartFormData.getParts(request, request, contentType, new MultiPartConfig.Builder().build()).forEach(parts -> {
                Map<String, String> pathParams = route.extractPathParams(path);
                RouteContext context = new RouteContext(request, response, callback, pathParams);
                runRoute(route, request, response, callback, context);
            });
            return true;
        }

        Map<String, String> pathParams = route.extractPathParams(path);
        RouteContext context = new RouteContext(request, response, callback, pathParams);
        runRoute(route, request, response, callback, context);
        return true;
    }

    private void runRoute(Route route, Request request, Response response, Callback callback, RouteContext context) {
        RouteBeforeModule beforeModule = router.getModule(RouteBeforeModule.class);
        if (beforeModule != null) {
            AbstractRouteResponse early = beforeModule.handle(context);
            if (early != null) {
                write(response, callback, early);
                return;
            }
        }

        AbstractRouteResponse routeResponse = route.execute(request, context);
        RouteAfterModule afterModule = router.getModule(RouteAfterModule.class);

        if (afterModule != null)
            routeResponse = afterModule.handle(context, routeResponse);

        write(response, callback, routeResponse);
    }

    private void write(Response response, Callback callback, AbstractRouteResponse routeResponse) {
        response.setStatus(routeResponse.getStatus().getCode());
        response.getHeaders().put("Content-Type", routeResponse.getContentType());
        response.write(true, ByteBuffer.wrap(routeResponse.getBody().getBytes(StandardCharsets.UTF_8)), callback);
    }

}