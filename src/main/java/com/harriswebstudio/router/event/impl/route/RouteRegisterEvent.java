package com.harriswebstudio.router.event.impl.route;

import com.harriswebstudio.router.event.Event;
import com.harriswebstudio.router.route.Route;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RouteRegisterEvent extends Event {

    private final Route route;

}