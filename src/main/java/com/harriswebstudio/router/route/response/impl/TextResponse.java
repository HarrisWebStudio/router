package com.harriswebstudio.router.route.response.impl;

import com.harriswebstudio.router.http.HttpStatus;
import com.harriswebstudio.router.route.response.AbstractRouteResponse;
import lombok.Getter;

@Getter
public class TextResponse extends AbstractRouteResponse {

    private final String body;

    public TextResponse(HttpStatus status, String body) {
        super(status, "text/plain");
        this.body = body;
    }

}

