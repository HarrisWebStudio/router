package com.harriswebstudio.router.event.impl.route;

import com.harriswebstudio.router.event.Cancellable;
import com.harriswebstudio.router.event.Event;
import com.harriswebstudio.router.route.response.AbstractRouteResponse;
import com.harriswebstudio.router.route.response.impl.ErrorResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.eclipse.jetty.server.Request;

@Getter
@RequiredArgsConstructor
public class PreRouteServeEvent extends Event implements Cancellable {

    private final String path;
    private final Request request;
    private final String requestIp;

    @Setter
    private boolean cancelled;
    private AbstractRouteResponse cancelledResponse;

    public void cancel(AbstractRouteResponse cancelledResponse) {
        this.cancelled = true;
        this.cancelledResponse = cancelledResponse;
    }

}