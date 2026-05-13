package com.harriswebstudio.router.route.response.impl;

import com.google.gson.JsonObject;
import com.harriswebstudio.router.Router;
import com.harriswebstudio.router.http.HttpStatus;
import com.harriswebstudio.router.route.response.AbstractRouteResponse;
import com.harriswebstudio.router.route.response.mav.ModelAndView;
import org.thymeleaf.context.Context;

public class ErrorResponse extends AbstractRouteResponse {

    private final String message;

    public ErrorResponse(HttpStatus status, String message) {
        super(status, "application/json");
        this.message = message;
    }

    @Override
    public String getBody() {
        if (Router.get().getRouteHandler().isMavInitialized()) {
            ModelAndView view = new ModelAndView("error");

            view.put("code", getStatus().getCode());
            view.put("message", message);

            return Router.get().getRouteHandler().getMavImplementation().process(view);
        }

        JsonObject object = new JsonObject();
        object.addProperty("message", message);
        object.addProperty("code", getStatus().getCode());

        return JsonResponse.GSON.toJson(object);
    }

    @Override
    public String getContentType() {
        return Router.get().getRouteHandler().isMavInitialized()
                ? "text/html; charset=UTF-8" : "application/json";
    }

}