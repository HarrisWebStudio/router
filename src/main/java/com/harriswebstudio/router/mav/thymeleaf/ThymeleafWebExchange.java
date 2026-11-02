package com.harriswebstudio.router.mav.thymeleaf;

import com.harriswebstudio.router.route.RouteContext;
import com.harriswebstudio.router.session.Session;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.http.HttpField;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.IWebRequest;
import org.thymeleaf.web.IWebSession;

import java.security.Principal;
import java.util.*;

@RequiredArgsConstructor
public class ThymeleafWebExchange implements IWebExchange, IWebRequest, IWebSession {

    private final RouteContext context;

    @Override
    public IWebRequest getRequest() {
        return this;
    }

    @Override
    public IWebSession getSession() {
        return this;
    }

    @Override
    public IWebApplication getApplication() {
        return null;
    }

    @Override
    public Principal getPrincipal() {
        return null;
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public String getContentType() {
        return "text/html; charset=UTF-8";
    }

    @Override
    public String getCharacterEncoding() {
        return "UTF-8";
    }

    @Override
    public String transformURL(String url) {
        String lowerUrl = url.toLowerCase();

        if (lowerUrl.startsWith("/css")
                || lowerUrl.startsWith("/js")
                || lowerUrl.startsWith("/image"))
            return "/assets" + url;

        return url;
    }

    @Override
    public String getMethod() {
        return context.getRawMethod();
    }

    @Override
    public String getScheme() {
        return context.getRequest().getHttpURI().getScheme();
    }

    @Override
    public String getServerName() {
        return context.getRequest().getHttpURI().getHost();
    }

    @Override
    public Integer getServerPort() {
        return context.getRequest().getHttpURI().getPort();
    }

    @Override
    public String getApplicationPath() {
        return "";
    }

    @Override
    public String getPathWithinApplication() {
        return context.getPath();
    }

    @Override
    public String getQueryString() {
        return context.getRequest().getHttpURI().getQuery();
    }

    // --- Headers ---

    @Override
    public boolean containsHeader(String name) {
        return context.getHeader(name) != null;
    }

    @Override
    public int getHeaderCount() {
        return (int) context.getRequest().getHeaders().stream().count();
    }

    @Override
    public Set<String> getAllHeaderNames() {
        Set<String> names = new LinkedHashSet<>();
        for (HttpField field : context.getRequest().getHeaders())
            names.add(field.getName());
        return names;
    }

    @Override
    public Map<String, String[]> getHeaderMap() {
        Map<String, String[]> map = new LinkedHashMap<>();
        for (String name : getAllHeaderNames())
            map.put(name, getHeaderValues(name));
        return map;
    }

    @Override
    public String getHeaderValue(String name) {
        return context.getHeader(name);
    }

    @Override
    public String[] getHeaderValues(String name) {
        return context.getRequest().getHeaders().getValuesList(name).toArray(new String[0]);
    }

    @Override
    public boolean containsParameter(String name) {
        return context.getQueryParams().containsKey(name);
    }

    @Override
    public int getParameterCount() {
        return context.getQueryParams().size();
    }

    @Override
    public Set<String> getAllParameterNames() {
        return context.getQueryParams().keySet();
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = new LinkedHashMap<>();
        context.getQueryParams().forEach((k, v) -> map.put(k, new String[]{v}));
        return map;
    }

    @Override
    public String getParameterValue(String name) {
        return context.getQueryParam(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        String val = context.getQueryParam(name);
        return val != null ? new String[]{val} : new String[0];
    }

    @Override
    public boolean containsCookie(String name) {
        return context.getCookie(name) != null;
    }

    @Override
    public int getCookieCount() {
        return context.getCookies().size();
    }

    @Override
    public Set<String> getAllCookieNames() {
        return context.getCookies().keySet();
    }

    @Override
    public Map<String, String[]> getCookieMap() {
        Map<String, String[]> map = new LinkedHashMap<>();
        context.getCookies().forEach((k, v) -> map.put(k, new String[]{v.getValue()}));
        return map;
    }

    @Override
    public String[] getCookieValues(String name) {
        String val = context.getCookie(name);
        return val != null ? new String[]{val} : new String[0];
    }

    private Session session() {
        return context.getSession();
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean containsAttribute(String name) {
        return session().has(name);
    }

    @Override
    public int getAttributeCount() {
        return session().getAttributes().size();
    }

    @Override
    public Set<String> getAllAttributeNames() {
        return session().getAttributes().keySet();
    }

    @Override
    public Map<String, Object> getAttributeMap() {
        return session().getAttributes();
    }

    @Override
    public Object getAttributeValue(String name) {
        return session().get(name);
    }

    @Override
    public void setAttributeValue(String name, Object value) {
        session().set(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        session().remove(name);
    }

}
