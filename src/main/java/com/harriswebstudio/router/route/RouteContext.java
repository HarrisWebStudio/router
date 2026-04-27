package com.harriswebstudio.router.route;

import com.harriswebstudio.router.route.multipart.MultipartFile;
import com.harriswebstudio.router.session.Session;
import com.harriswebstudio.router.session.SessionStore;
import lombok.Getter;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.http.MultiPart;
import org.eclipse.jetty.http.MultiPartFormData;
import org.eclipse.jetty.io.Content;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class RouteContext {

    private final Request request;
    private final Response response;
    private final Callback callback;
    private final Map<String, String> pathParams;
    private final Map<String, String> queryParams;

    private Map<String, String> formFields;
    private List<MultipartFile> multipartFiles;
    private Session session;

    public RouteContext(Request request, Response response, Callback callback, Map<String, String> pathParams) {
        this.request = request;
        this.response = response;
        this.callback = callback;
        this.pathParams = pathParams;
        this.queryParams = parseQuery(request.getHttpURI().getQuery());
    }

    public void sendRedirect(String location) {
        sendRedirect(location, 302);
    }

    public void sendRedirect(String location, int statusCode) {
        response.setStatus(statusCode);
        response.getHeaders().put("Location", location);
        response.write(true, ByteBuffer.allocate(0), callback);
    }

    public String getCookie(String name) {
        List<HttpCookie> cookies = Request.getCookies(request);
        if (cookies == null) return null;
        return cookies.stream()
                .filter(cookie -> cookie.getName().equals(name))
                .map(HttpCookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public Map<String, HttpCookie> getCookies() {
        Map<String, HttpCookie> map = new HashMap<>();
        List<HttpCookie> cookies = Request.getCookies(request);

        if (cookies != null)
            cookies.forEach(cookie -> map.put(cookie.getName(), cookie));

        return map;
    }

    public void setCookie(String name, String value) {
        setCookie(name, value, -1, "/", false, true);
    }

    public void setCookie(String name, String value, int maxAgeSeconds) {
        setCookie(name, value, maxAgeSeconds, "/", false, true);
    }

    public void setCookie(String name, String value, int maxAgeSeconds, String path, boolean secure, boolean httpOnly) {
        HttpCookie cookie = HttpCookie.build(name, value)
                .path(path)
                .maxAge(maxAgeSeconds)
                .secure(secure)
                .httpOnly(httpOnly)
                .build();

        Response.addCookie(response, cookie);
    }

    public void clearCookie(String name) {
        setCookie(name, "", 0);
    }

    public Session getSession() {
        if (session != null)
            return session;

        String id = getCookie(SessionStore.SESSION_COOKIE);
        session = SessionStore.getOrCreate(id);

        if (id == null || !id.equals(session.getId()))
            setCookie(SessionStore.SESSION_COOKIE, session.getId(), -1, "/", false, true);

        return session;
    }

    public void destroySession() {
        String id = getCookie(SessionStore.SESSION_COOKIE);
        if (id != null)
            SessionStore.destroy(id);

        clearCookie(SessionStore.SESSION_COOKIE);
        session = null;
    }

    public String getFormField(String name) {
        return getFormFields().get(name);
    }

    public Map<String, String> getFormFields() {
        if (formFields != null)
            return formFields;

        formFields = new HashMap<>();

        try {
            Fields fields = Request.getParameters(request);
            if (fields != null)
                fields.forEach(field -> formFields.put(field.getName(), field.getValue()));
        } catch (Exception ignored) {
        }

        return formFields;
    }

    public List<MultipartFile> getFiles() {
        if (multipartFiles != null)
            return multipartFiles;

        multipartFiles = new ArrayList<>();

        try {
            MultiPartFormData.Parts parts = (MultiPartFormData.Parts) request.getAttribute(MultiPartFormData.class.getName());
            if (parts == null)
                return multipartFiles;

            for (MultiPart.Part part : parts) {
                if (part.getFileName() == null)
                    continue;

                byte[] bytes;
                try (InputStream inputStream = Content.Source.asInputStream((Content.Source) part)) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    inputStream.transferTo(out);
                    bytes = out.toByteArray();
                }

                multipartFiles.add(new MultipartFile(
                        part.getName(),
                        part.getFileName(),
                        part.getHeaders().get("Content-Type"),
                        bytes
                ));
            }
        } catch (Exception ignored) {
        }

        return multipartFiles;
    }

    public MultipartFile getFile(String fieldName) {
        return getFiles().stream()
                .filter(f -> f.getFieldName().equals(fieldName))
                .findFirst()
                .orElse(null);
    }

    public String getIpAddress() {
        String forwarded = request.getHeaders().get("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty())
            return forwarded.split(",")[0].trim();

        String realIp = request.getHeaders().get("X-Real-IP");
        if (realIp != null && !realIp.isEmpty())
            return realIp.trim();

        SocketAddress address = request.getConnectionMetaData().getRemoteSocketAddress();
        if (address instanceof InetSocketAddress inet)
            return inet.getAddress().getHostAddress();

        return address != null ? address.toString() : null;
    }

    public String getRawMethod() {
        return request.getMethod();
    }

    public HttpMethod getMethod() {
        return HttpMethod.fromString(request.getMethod());
    }

    public String getPath() {
        return request.getHttpURI().getPath();
    }

    public String getHeader(String name) {
        return request.getHeaders().get(name);
    }

    public Map<String, String> getHeaders() {
        Map<String, String> map = new HashMap<>();
        for (HttpField field : request.getHeaders())
            map.put(field.getName(), field.getValue());
        return map;
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public String getQueryParam(String name, String defaultValue) {
        return queryParams.getOrDefault(name, defaultValue);
    }

    public String getPathParam(String name) {
        return pathParams.get(name);
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty())
            return map;

        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0)
                map.put(pair.substring(0, idx), pair.substring(idx + 1));
            else
                map.put(pair, "");
        }

        return map;
    }

}
