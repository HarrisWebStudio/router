package com.harriswebstudio.router.module.impl.error;

import com.harriswebstudio.router.route.response.AbstractRouteResponse;

@FunctionalInterface
public interface ErrorHandler<T extends AbstractRouteResponse> {

    T handleError(Exception exception);

}