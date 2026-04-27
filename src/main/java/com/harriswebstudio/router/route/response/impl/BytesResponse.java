package com.harriswebstudio.router.route.response.impl;

import com.harriswebstudio.router.http.HttpStatus;
import com.harriswebstudio.router.route.response.AbstractRouteResponse;
import lombok.Getter;

@Getter
public class BytesResponse extends AbstractRouteResponse {

    private final byte[] bytes;

    public BytesResponse(HttpStatus status, String contentType, byte[] bytes) {
        super(status, contentType);
        this.bytes = bytes;
    }

    @Override
    public String getBody() {
        return "";
    }

}

