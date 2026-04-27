package com.harriswebstudio.router.route.response.mav;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class ModelAndView {

    private final String viewName;
    private final Map<String, Object> model = new HashMap<>();

    public ModelAndView put(String key, Object value) {
        model.put(key, value);
        return this;
    }

    public Map<String, Object> getModel() {
        return Collections.unmodifiableMap(model);
    }

}