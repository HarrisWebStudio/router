package com.harriswebstudio.router.route.multipart;

public record MultipartFile(String fieldName, String fileName, String contentType, byte[] bytes) {

    public long getSize() {
        return bytes.length;
    }

    public String getContentAsString() {
        return new String(bytes);
    }

}