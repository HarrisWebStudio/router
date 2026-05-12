package com.harriswebstudio.router.route.response;

import com.harriswebstudio.router.http.HttpStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractRouteResponse {

    private HttpStatus status;
    private String contentType;

    private final Map<String, String> headers = new HashMap<>();

    public abstract String getBody();

    public <T extends AbstractRouteResponse> T withHeader(String name, String value) {
        headers.put(name, value);
        return (T) this;
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

}