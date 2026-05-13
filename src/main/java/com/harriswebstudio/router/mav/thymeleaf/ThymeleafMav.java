package com.harriswebstudio.router.mav.thymeleaf;

import com.harriswebstudio.router.mav.AbstractMav;
import com.harriswebstudio.router.route.response.mav.ModelAndView;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class ThymeleafMav extends AbstractMav {

    private final TemplateEngine templateEngine;

    public ThymeleafMav() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        this.templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        templateEngine.addDialect(new LayoutDialect());
    }

    @Override
    public String process(ModelAndView view) {
        Context context = new Context();
        context.setVariables(view.getModel());

        return templateEngine.process(view.getViewName(), context);
    }

}