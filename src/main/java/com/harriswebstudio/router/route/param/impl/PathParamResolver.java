package com.harriswebstudio.router.route.param.impl;

import com.harriswebstudio.router.route.RouteContext;
import com.harriswebstudio.router.route.annotation.parameter.PathParam;
import com.harriswebstudio.router.route.param.ParamRegistry;
import com.harriswebstudio.router.route.param.ParamResolver;
import org.eclipse.jetty.server.Request;

import java.lang.reflect.Parameter;

public class PathParamResolver implements ParamResolver {

    @Override
    public Object resolve(Parameter param, Request request, RouteContext context) {
        String name = param.getAnnotation(PathParam.class).value();
        return ParamRegistry.convert(context.getPathParam(name), param.getType());
    }

}