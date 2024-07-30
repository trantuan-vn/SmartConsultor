package com.smartconsultor.cluster;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.web.handler.impl.OAuth2AuthHandlerImpl;

public class GatewayOAuth2AuthHandlerImpl extends OAuth2AuthHandlerImpl {
  public GatewayOAuth2AuthHandlerImpl(Vertx vertx, OAuth2Auth authProvider, String callbackURL) {
    super(vertx, authProvider, callbackURL);
  }    
}
