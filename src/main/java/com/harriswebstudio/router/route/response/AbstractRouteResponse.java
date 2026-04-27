package com.harriswebstudio.router.route.response;

import com.harriswebstudio.router.http.HttpStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class AbstractRouteResponse {

    private final HttpStatus status;
    private final String contentType;

    public abstract String getBody();

}