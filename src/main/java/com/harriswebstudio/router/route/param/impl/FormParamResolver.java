package com.harriswebstudio.router.route.param.impl;

import com.harriswebstudio.router.route.RouteContext;
import com.harriswebstudio.router.route.annotation.parameter.FormParam;
import com.harriswebstudio.router.route.param.ParamRegistry;
import com.harriswebstudio.router.route.param.ParamResolver;
import org.eclipse.jetty.server.Request;

import java.lang.reflect.Parameter;

public class FormParamResolver implements ParamResolver {

    @Override
    public Object resolve(Parameter param, Request request, RouteContext context) {
        String name = param.getAnnotation(FormParam.class).value();
        return ParamRegistry.convert(context.getFormField(name), param.getType());
    }

}