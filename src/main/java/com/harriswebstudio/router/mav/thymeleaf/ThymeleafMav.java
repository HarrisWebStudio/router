package com.harriswebstudio.router.mav.thymeleaf;

import com.harriswebstudio.router.mav.AbstractMav;
import com.harriswebstudio.router.route.RouteContext;
import com.harriswebstudio.router.route.response.mav.ModelAndView;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class ThymeleafMav extends AbstractMav {

    private static final TemplateEngine ENGINE;
    private static final ClassLoaderTemplateResolver RESOLVER;

    static {
        RESOLVER = new ClassLoaderTemplateResolver();
        RESOLVER.setPrefix("templates/");
        RESOLVER.setSuffix(".html");
        RESOLVER.setTemplateMode(TemplateMode.HTML);
        RESOLVER.setCharacterEncoding("UTF-8");
        RESOLVER.setCacheable(false);

        ENGINE = new TemplateEngine();
        ENGINE.setTemplateResolver(RESOLVER);
        ENGINE.addDialect(new LayoutDialect());
    }

    @Override
    public String process(ModelAndView view, RouteContext context) {
        if (context == null)
            return ENGINE.process(view.getViewName(), new Context());

        ThymeleafWebExchange exchange = new ThymeleafWebExchange(context);
        WebContext webContext = new WebContext(exchange, exchange.getLocale(), view.getModel());

        return ENGINE.process(view.getViewName(), webContext);
    }

}