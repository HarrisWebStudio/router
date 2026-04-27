package com.harriswebstudio.router.route.response.mav;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class ThymeleafEngine {

    private static final TemplateEngine ENGINE;

    static {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);

        ENGINE = new TemplateEngine();
        ENGINE.setTemplateResolver(resolver);
    }

    public static TemplateEngine get() {
        return ENGINE;
    }

}

