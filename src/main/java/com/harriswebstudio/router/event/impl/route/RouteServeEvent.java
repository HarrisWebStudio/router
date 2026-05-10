package com.harriswebstudio.router.event.impl.route;

import com.harriswebstudio.router.event.Event;
import com.harriswebstudio.router.route.Route;
import com.harriswebstudio.router.route.RouteContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RouteServeEvent extends Event {

    private final Route route;
    private final RouteContext routeContext;

}