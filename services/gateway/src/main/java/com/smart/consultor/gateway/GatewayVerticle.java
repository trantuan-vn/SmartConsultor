package com.smart.consultor.gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.providers.FacebookAuth;
import io.vertx.ext.auth.oauth2.providers.GoogleAuth;
import io.vertx.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CSPHandler;
import io.vertx.ext.web.handler.CSRFHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.HSTSHandler;
import io.vertx.ext.web.handler.MultiTenantHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.XFrameHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GatewayVerticle extends AbstractVerticle {
  private static final int DEFAULT_PORT = 8787;
  private static final Logger logger = LoggerFactory.getLogger(GatewayVerticle.class);
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    super.start();

    // get HTTP host and port from configuration, or use default value
    String host = config().getString("api.gateway.http.address", "localhost");
    int port = config().getInteger("api.gateway.http.port", DEFAULT_PORT);
    String baseUrl = String.format("https://%s:%d", host, port);

    Router router = Router.router(vertx);

    // body handler
    router.route().handler(BodyHandler.create());
    // Store session information on the server side
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    // CSRF handler setup required for logout form
    String csrfSecret = generateCsrfSecret();
    router.route().handler(CSRFHandler.create(vertx,csrfSecret));
    //HSTS Handler
    router.route().handler(HSTSHandler.create());
    // CSP handler
    router.route().handler(
      CSPHandler.create()
        .addDirective("default-src", "*.smartconsultor.com"));
    // XFrame handler
    router.route().handler(XFrameHandler.create(XFrameHandler.DENY));
    // Cors Handler
    enableCorsSupport(router);
    // static content
    router.route("/static/*").handler(StaticHandler.create());
    // create oath2 instance for google
    OAuth2Auth googleAuthProvider = GoogleAuth.create(vertx, 
            "533670103-3iajdsvqgl70ampdmslspn6268gedbq6.apps.googleusercontent.com",
            "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");

    // create a oauth2 handler on our domain: "http://localhost:8080"
    OAuth2AuthHandler googleOAuth2 = OAuth2AuthHandler.create(
      vertx,
      googleAuthProvider,
      baseUrl+"/google-callback");
    // setup the callback handler for receiving the Google callback
    googleOAuth2.setupCallback(router.route("/google-callback"));   
    
    // create oath2 instance for facebook
    OAuth2Auth facebookAuthProvider = FacebookAuth.create(vertx, 
            "745909260767468",
            "aa1a6de83dee32c55aa97da02305cc94");
    // Create an OAuth2 handler for Facebook
    OAuth2AuthHandler facebookOAuth2 = OAuth2AuthHandler.create(
      vertx,
      facebookAuthProvider,
      baseUrl+"/facebook-callback");
    // create oath2 instance for keycloak
    
    OAuth2Auth keycloakAuthProvider = KeycloakAuth.create(vertx,config());

    router.route("/api/*").handler(
      MultiTenantHandler.create("X-Tenant")
        // tenants using facebook should go this way:
        .addTenantHandler("facebook", facebookOAuth2)
        // tenants using google should go this way:
        .addTenantHandler("google", googleOAuth2)
        // tenants using google should go this way:
        .addTenantHandler("keycloak", context -> protectByKeycloak(keycloakAuthProvider,context))        
        // tenants using webauthn should go this way:
        //.addTenantHandler("webauthn", webAuthnHandler)        
        // all other should be forbidden
        .addDefaultHandler(ctx -> ctx.fail(401)));    

    router.get("/uaa").handler(this::authUaaHandler);

    String hostURI=buildHostURI();

    router.get("/login").handler(
      MultiTenantHandler.create("X-Tenant")
        // tenants using facebook should go this way:
        .addTenantHandler("facebook", context -> loginEntryHandler(facebookAuthProvider,hostURI,context))
        // tenants using google should go this way:
        .addTenantHandler("google", context -> loginEntryHandler(googleAuthProvider,hostURI,context))
        // tenants using keycloak should go this way:
        .addTenantHandler("keycloak", context -> loginByKeycloak(keycloakAuthProvider,context))             
        // tenants using webauthn should go this way:
        //.addTenantHandler("webauthn", webAuthnHandler)        
        // all other should be forbidden
        .addDefaultHandler(ctx -> ctx.fail(401)));
    router.post("/logout").handler(this::logoutHandler);  
    
    // set auth callback handler
    router.route("/facebook-callback").handler(context -> authCallback(facebookAuthProvider, hostURI, context));
    router.route("/google-callback").handler(context -> authCallback(googleAuthProvider, hostURI, context));    

    // create http server
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(port, host, ar -> {
        if (ar.succeeded()) {
          //publishApiGateway(host, port);
          startPromise.complete();
          logger.info("API Gateway is running on port " + port);
          // publish log
          //publishGatewayLog("api_gateway_init_success:" + port);
        } else {
          startPromise.fail(ar.cause());
        }
      });
  }


  // Uaa
  private void authUaaHandler(RoutingContext context) {
    if (context.user() != null) {
      JsonObject principal = context.user().principal();
      if (principal == null) {

      } else {

      }
    } else {
      context.fail(401);
    }
  }

  // google, facebook
  private void loginEntryHandler(OAuth2Auth oauth2, String hostURL, RoutingContext context) {
    // Create the OAuth2 authorization URL
    @SuppressWarnings("deprecation")
    String authorizationURI = oauth2.authorizeURL(new JsonObject()
        .put("redirect_uri", hostURL + context.request().path())
        .put("scope", "profile email")
        .put("state", "some_state_value"));

    // Redirect the user to the OAuth2 authorization URL
    context.response()
        .putHeader("Location", authorizationURI)
        .setStatusCode(302)
        .end();
  }
  @SuppressWarnings("deprecation")
  private void authCallback(OAuth2Auth oauth2, String hostURL, RoutingContext context) {
    // Extract the authorization code from the callback
    String code = context.request().getParam("code");

    if (code == null) {
        context.fail(400); // Bad Request
        return;
    }

    // Prepare the token request payload
    JsonObject tokenRequestPayload = new JsonObject()
        .put("code", code)
        .put("redirect_uri", hostURL + context.request().path())
        .put("grant_type", "authorization_code");

    // Exchange the authorization code for tokens
    oauth2.authenticate(tokenRequestPayload, res -> {
        if (res.succeeded()) {
            User user = res.result();

            // Here you can fetch user details using the user token if needed
            // For example:
            // String accessToken = user.principal().getString("access_token");

            // Add user to the session
            context.setUser(user);
            // Fetch user details using the user token if needed
            JsonObject userInfo = user.principal();
            // Return user information as JSON response
            context.response()
                .putHeader("Content-Type", "application/json")
                .end(userInfo.encode());
        } else {
            // Authentication failed
            context.fail(res.cause());
        }
    });
  }  
  // logout
  private void logoutHandler(RoutingContext context) {
    context.clearUser();
    context.session().destroy();
    context.response().setStatusCode(204).end();
  }

  // keycloak
  @SuppressWarnings("deprecation")
  private void loginByKeycloak(OAuth2Auth keycloakAuthProvider, RoutingContext context) {
    JsonObject credentials = context.getBodyAsJson();
    String username = credentials.getString("username");
    String password = credentials.getString("password");

    JsonObject authInfo = new JsonObject()
        .put("username", username)
        .put("password", password);

    keycloakAuthProvider.authenticate(authInfo, res -> {
        if (res.succeeded()) {
            User user = res.result();
            context.setUser(user);
            // Fetch user details using the user token if needed
            JsonObject userInfo = user.principal();
            // Return user information as JSON response
            context.response()
                .putHeader("Content-Type", "application/json")
                .end(userInfo.encode());
        } else {
            context.response().setStatusCode(401).end("Login failed");
        }
    });        
  }

  @SuppressWarnings("deprecation")
  private void protectByKeycloak(OAuth2Auth oauth2, RoutingContext ctx) {
    String authHeader = ctx.request().getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        ctx.fail(401);
        return;
    }

    String token = authHeader.substring("Bearer ".length());

    oauth2.authenticate(new JsonObject().put("access_token", token), res -> {
        if (res.succeeded()) {
            User user = res.result();
            ctx.setUser(user);
            ctx.next();
        } else {
            ctx.fail(401);
        }
    });
  }
  
  // utils
  private String buildHostURI() {
    int port = config().getInteger("api.gateway.http.port", DEFAULT_PORT);
    final String host = config().getString("api.gateway.http.address.external", "localhost");
    return String.format("https://%s:%d", host, port);
  }  

  private static String generateCsrfSecret() {
      try {
          // Sử dụng SecureRandom để tạo một salt ngẫu nhiên
          SecureRandom random = new SecureRandom();
          byte[] salt = new byte[16];
          random.nextBytes(salt);

          // Tạo một CSRF secret từ salt và thời gian hiện tại
          String csrfSeed = Base64.getEncoder().encodeToString(salt) + System.currentTimeMillis();

          // Sử dụng SHA-256 để băm CSRF secret
          MessageDigest digest = MessageDigest.getInstance("SHA-256");
          byte[] hash = digest.digest(csrfSeed.getBytes());

          // Chuyển đổi kết quả băm thành một chuỗi hex
          StringBuilder hexString = new StringBuilder();
          for (byte b : hash) {
              String hex = Integer.toHexString(0xff & b);
              if (hex.length() == 1) hexString.append('0');
              hexString.append(hex);
          }

          return hexString.toString();
      } catch (NoSuchAlgorithmException e) {
          e.printStackTrace();
          return null;
      }
  } 
  private void enableCorsSupport(Router router) {
    Set<String> allowHeaders = new HashSet<>();
    allowHeaders.add("x-requested-with");
    allowHeaders.add("Access-Control-Allow-Origin");
    allowHeaders.add("origin");
    allowHeaders.add("Content-Type");
    allowHeaders.add("accept");
    Set<HttpMethod> allowMethods = new HashSet<>();
    allowMethods.add(HttpMethod.GET);
    allowMethods.add(HttpMethod.PUT);
    allowMethods.add(HttpMethod.OPTIONS);
    allowMethods.add(HttpMethod.POST);
    allowMethods.add(HttpMethod.DELETE);
    allowMethods.add(HttpMethod.PATCH);

    router.route().handler(CorsHandler.create()
      .allowedHeaders(allowHeaders)
      .allowedMethods(allowMethods));
  }    
}
