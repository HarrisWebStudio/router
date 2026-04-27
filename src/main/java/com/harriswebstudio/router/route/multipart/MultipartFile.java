package com.harriswebstudio.router.route.multipart;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MultipartFile {

    private final String fieldName;
    private final String fileName;
    private final String contentType;
    private final byte[] bytes;

    public long getSize() {
        return bytes.length;
    }

    public String getContentAsString() {
        return new String(bytes);
    }

}