package com.harriswebstudio.router.mav;

import com.harriswebstudio.router.route.RouteContext;
import com.harriswebstudio.router.route.response.mav.ModelAndView;

public abstract class AbstractMav {

    public abstract String process(ModelAndView view, RouteContext context);

}