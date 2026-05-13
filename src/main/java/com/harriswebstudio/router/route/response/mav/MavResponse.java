package com.harriswebstudio.router.route.response.mav;

import com.harriswebstudio.router.Router;
import com.harriswebstudio.router.http.HttpStatus;
import com.harriswebstudio.router.route.response.AbstractRouteResponse;

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
        return Router.get().getRouteHandler().getMavImplementation().process(view);
    }

}