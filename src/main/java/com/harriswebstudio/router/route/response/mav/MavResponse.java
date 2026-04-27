package com.harriswebstudio.router.route.response.mav;

import com.harriswebstudio.router.http.HttpStatus;
import com.harriswebstudio.router.route.response.AbstractRouteResponse;
import org.thymeleaf.context.Context;

public class MavResponse extends AbstractRouteResponse {

    private final ModelAndView view;

    public MavResponse(HttpStatus status, ModelAndView view) {
        super(status, "text/html; charset=UTF-8");
        this.view = view;
    }

    public MavResponse(ModelAndView view) {
        this(HttpStatus.OK, view);
    }

    @Override
    public String getBody() {
        Context context = new Context();
        context.setVariables(view.getModel());

        return ThymeleafEngine.get().process(view.getViewName(), context);
    }

}