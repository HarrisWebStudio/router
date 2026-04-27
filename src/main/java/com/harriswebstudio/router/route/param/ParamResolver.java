package com.harriswebstudio.router.route.param;

import com.harriswebstudio.router.route.RouteContext;
import org.eclipse.jetty.server.Request;

import java.lang.reflect.Parameter;

@FunctionalInterface
public interface ParamResolver {

    Object resolve(Parameter param, Request request, RouteContext context);

}