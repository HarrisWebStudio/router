package com.harriswebstudio.router.route;

import com.harriswebstudio.router.route.response.AbstractRouteResponse;

@FunctionalInterface
public interface RouteFunction {

    AbstractRouteResponse handle(RouteContext context);

}