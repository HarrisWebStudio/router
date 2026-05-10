package com.harriswebstudio.router.route.response.mav;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
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
        resolver.setCacheable(false);

        ENGINE = new TemplateEngine();
        ENGINE.setTemplateResolver(resolver);
        ENGINE.addDialect(new LayoutDialect());
    }

    public static TemplateEngine get() {
        return ENGINE;
    }

}
