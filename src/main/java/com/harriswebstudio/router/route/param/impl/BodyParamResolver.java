package com.harriswebstudio.router.route.param.impl;

import com.harriswebstudio.router.Router;
import com.harriswebstudio.router.route.RouteContext;
import com.harriswebstudio.router.route.param.ParamResolver;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;

import java.lang.reflect.Parameter;

public class BodyParamResolver implements ParamResolver {

    @Override
    public Object resolve(Parameter param, Request request, RouteContext context) {
        try {
            String body = Content.Source.asString(request);
            return Router.GSON.fromJson(body, param.getType());
        } catch (Exception exception) {
            throw new RuntimeException("Failed to read request body", exception);
        }
    }

}