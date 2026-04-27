package com.harriswebstudio.router.module.impl;

import com.harriswebstudio.router.module.RouterModule;
import com.harriswebstudio.router.route.RouteContext;
import com.harriswebstudio.router.route.response.AbstractRouteResponse;

public abstract class RouteAfterModule implements RouterModule {

    public abstract AbstractRouteResponse handle(RouteContext context, AbstractRouteResponse response);

}