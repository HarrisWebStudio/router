package com.harriswebstudio.router.server;

import com.harriswebstudio.router.Router;
import com.harriswebstudio.router.event.impl.route.PreRouteServeEvent;
import com.harriswebstudio.router.event.impl.route.PostRouteServeEvent;
import com.harriswebstudio.router.event.impl.route.RouteServeEvent;
import com.harriswebstudio.router.http.HttpStatus;
import com.harriswebstudio.router.module.impl.assets.AssetsModule;
import com.harriswebstudio.router.module.impl.RouteAfterModule;
import com.harriswebstudio.router.module.impl.RouteBeforeModule;
import com.harriswebstudio.router.route.Route;
import com.harriswebstudio.router.route.RouteContext;
import com.harriswebstudio.router.route.response.AbstractRouteResponse;
import com.harriswebstudio.router.route.response.impl.BytesResponse;
import com.harriswebstudio.router.route.response.impl.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MultiPartConfig;
import org.eclipse.jetty.http.MultiPartFormData;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RequiredArgsConstructor
public class ServerHandler extends Handler.Abstract {

    private final Router router;

    @Override
    public boolean handle(Request request, Response response, Callback callback) {
        long requestStartTime = System.currentTimeMillis();
        AssetsModule assetsModule = router.getModule(AssetsModule.class);
        if (assetsModule != null && assetsModule.handle(request, response, callback))
            return true;

        String path = request.getHttpURI().getPath();
        Route route = router.getRouteHandler().getRoute(
                HttpMethod.fromString(request.getMethod()), path);

        PreRouteServeEvent preServeEvent = router.getEventHandler().callEvent(new PreRouteServeEvent(
                path,
                request,
                getRealIp(request)
        ));

        if (preServeEvent.isCancelled()) {
            write(null, null, request.getMethod(), requestStartTime, response, callback, preServeEvent.getCancelledResponse());
            return true;
        }

        if (route == null) {
            write(null, null, request.getMethod(), requestStartTime, response, callback, new ErrorResponse(HttpStatus.NOT_FOUND, "Not Found"));
            return true;
        }

        String contentType = request.getHeaders().get("Content-Type");
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            MultiPartFormData.getParts(request, request, contentType, new MultiPartConfig.Builder().build()).forEach(parts -> {
                Map<String, String> pathParams = route.extractPathParams(path);
                RouteContext context = new RouteContext(request, response, callback, pathParams);
                runRoute(route, request, requestStartTime, response, callback, context);
            });
            return true;
        }

        Map<String, String> pathParams = route.extractPathParams(path);
        RouteContext context = new RouteContext(request, response, callback, pathParams);
        runRoute(route, request, requestStartTime, response, callback, context);
        return true;
    }

    private void runRoute(Route route, Request request, long requestStartTime, Response response, Callback callback, RouteContext context) {
        RouteBeforeModule beforeModule = router.getModule(RouteBeforeModule.class);
        if (beforeModule != null) {
            AbstractRouteResponse early = beforeModule.handle(context);

            if (early != null) {
                write(null, context, route.getMethod().name(), requestStartTime, response, callback, early);
                return;
            }
        }

        AbstractRouteResponse routeResponse = route.execute(request, context);
        RouteAfterModule afterModule = router.getModule(RouteAfterModule.class);

        if (afterModule != null)
            routeResponse = afterModule.handle(context, routeResponse);

        router.getEventHandler().callEvent(new RouteServeEvent(route, context, routeResponse));
        write(route, context, route.getMethod().name(), requestStartTime, response, callback, routeResponse);
    }

    private void write(Route route, RouteContext context, String method,
                       long requestStartTime, Response response,
                       Callback callback, AbstractRouteResponse routeResponse) {
        response.setStatus(routeResponse.getStatus().getCode());
        response.getHeaders().put("Content-Type", routeResponse.getContentType());

        routeResponse.getHeaders().forEach((key, value) ->
                response.getHeaders().put(key, value));

        if (routeResponse instanceof BytesResponse bytesResponse) {
            response.write(true, ByteBuffer.wrap(bytesResponse.getBytes()), callback);
        } else {
            response.write(true, ByteBuffer.wrap(routeResponse.getBody(context).getBytes(StandardCharsets.UTF_8)), callback);
        }

        router.getEventHandler().callEvent(new PostRouteServeEvent(
                route,
                context,
                routeResponse,
                method,
                System.currentTimeMillis() - requestStartTime));
    }

    public String getRealIp(Request request) {
        if (request == null)
            return null;

        String forwardedForHeader = request.getHeaders().get("X-Forwarded-For");
        if (forwardedForHeader != null && !forwardedForHeader.isBlank())
            return forwardedForHeader.split(",")[0].trim();

        String realIp = request.getHeaders().get("X-Real-IP");
        if (realIp != null && !realIp.isEmpty())
            return realIp.trim();

        String forwardedHeader = request.getHeaders().get("Forwarded");
        if (forwardedHeader != null && !forwardedHeader.isBlank()) {
            for (String part : forwardedHeader.split(";")) {
                part = part.trim();

                if (part.startsWith("for="))
                    return part.substring(4).replace("\"", "");
            }
        }

        SocketAddress remote = request.getConnectionMetaData().getRemoteSocketAddress();
        if (remote instanceof InetSocketAddress socketAddress) {
            InetAddress address = socketAddress.getAddress();

            if (address != null)
                return address.getHostAddress();

            return socketAddress.getHostString();
        }

        return remote != null ? remote.toString() : null;
    }

}