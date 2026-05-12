package com.harriswebstudio.router.route.response.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.harriswebstudio.router.http.HttpStatus;
import com.harriswebstudio.router.route.response.AbstractRouteResponse;
import lombok.Getter;

@Getter
public class JsonResponse extends AbstractRouteResponse {

    public static final Gson GSON = new Gson();

    private final JsonElement element;

    public JsonResponse(HttpStatus status, JsonElement element) {
        super(status, "application/json");
        this.element = element;
    }

    @Override
    public String getBody() {
        return GSON.toJson(element);
    }

}