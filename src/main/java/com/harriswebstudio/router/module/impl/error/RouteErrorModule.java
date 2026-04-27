package com.harriswebstudio.router.module.impl.error;

import com.harriswebstudio.router.module.RouterModule;
import com.harriswebstudio.router.route.response.AbstractRouteResponse;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class RouteErrorModule<T extends AbstractRouteResponse> implements RouterModule {

    private final Function<Exception, T> errorHandler;

    public final T handle(Exception exception) {
        return errorHandler.apply(exception);
    }

}