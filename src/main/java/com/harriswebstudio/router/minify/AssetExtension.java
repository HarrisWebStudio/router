package com.harriswebstudio.router.minify;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AssetExtension {

    CSS(".css", "text/css"),
    JS(".js", "application/javascript"),
    PNG(".png", "image/png"),
    JPG(".jpg", "image/jpeg"),
    JPEG(".jpeg", "image/jpeg"),
    GIF(".gif", "image/gif"),
    SVG(".svg", "image/svg+xml"),
    ICO(".ico", "image/x-icon"),
    WEBP(".webp", "image/webp"),
    WOFF(".woff", "font/woff"),
    WOFF2(".woff2", "font/woff2"),
    TTF(".ttf", "font/ttf"),
    HTML(".html", "text/html"),
    JSON(".json", "application/json");

    private final String endsWith;
    private final String contentType;

    public static AssetExtension fromString(String input) {
        for (AssetExtension value : values()) {
            if (input.endsWith(value.getEndsWith()))
                return value;
        }

        return null;
    }

}