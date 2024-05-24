package com.smart.consultor.gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2Options;
//import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.providers.FacebookAuth;
import io.vertx.ext.auth.oauth2.providers.GoogleAuth;
import io.vertx.ext.auth.oauth2.providers.KeycloakAuth;
/* 
import io.vertx.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.ext.auth.webauthn.Attestation;
import io.vertx.ext.auth.webauthn.AuthenticatorTransport;
import io.vertx.ext.auth.webauthn.PublicKeyCredential;
import io.vertx.ext.auth.webauthn.RelyingParty;
import io.vertx.ext.auth.webauthn.UserVerification;
import io.vertx.ext.auth.webauthn.WebAuthn;
import io.vertx.ext.auth.webauthn.WebAuthnOptions;
*/
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CSPHandler;
import io.vertx.ext.web.handler.CSRFHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.HSTSHandler;
import io.vertx.ext.web.handler.MultiTenantHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
//import io.vertx.ext.web.handler.WebAuthnHandler;
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
    /*   
    // create the webauthn security object
    InMemoryStore database = new InMemoryStore();
    WebAuthn webAuthN = WebAuthn.create(
      vertx,
      new WebAuthnOptions()   // (1)
        .setRelyingParty(new RelyingParty()
          .setName("Vert.x FIDO2/webauthn"))
        .setUserVerification(UserVerification.DISCOURAGED)  
        .setAttestation(Attestation.NONE)   
        .setRequireResidentKey(false)   
        .setChallengeLength(64)   
        .addPubKeyCredParam(PublicKeyCredential.ES256)    
        .addPubKeyCredParam(PublicKeyCredential.RS256)
        .addTransport(AuthenticatorTransport.USB)   
        .addTransport(AuthenticatorTransport.NFC)
        .addTransport(AuthenticatorTransport.BLE)
        .addTransport(AuthenticatorTransport.INTERNAL))
      // where to load/update authenticators data
      .authenticatorFetcher(database::fetcher)
      .authenticatorUpdater(database::updater);    
    WebAuthnHandler webAuthnHandler = WebAuthnHandler.create(webAuthN)
      // required callback
      .setupCallback(router.post("/webauthn/callback"))
      // optional register callback
      .setupCredentialsCreateCallback(router.post("/webauthn/register"))
      // optional login callback
      .setupCredentialsGetCallback(router.post("/webauthn/login"));      
    */
    router.route("/api/*").handler(
      MultiTenantHandler.create("X-Tenant")
        // tenants using facebook should go this way:
        .addTenantHandler("facebook", facebookOAuth2)
        // tenants using google should go this way:
        .addTenantHandler("google", googleOAuth2)
        // tenants using webauthn should go this way:
        //.addTenantHandler("webauthn", webAuthnHandler)        
        // all other should be forbidden
        .addDefaultHandler(ctx -> ctx.fail(401)));    

    router.get("/uaa").handler(this::authUaaHandler);
    router.get("/login").handler(
      MultiTenantHandler.create("X-Tenant")
        // tenants using facebook should go this way:
        .addTenantHandler("facebook", context -> loginEntryHandler(facebookAuthProvider,context))
        // tenants using google should go this way:
        .addTenantHandler("google", context -> loginEntryHandler(googleAuthProvider,context))
        // tenants using keycloak should go this way:
        .addTenantHandler("keycloak", this::loginKeycloak)        
        // tenants using webauthn should go this way:
        //.addTenantHandler("webauthn", webAuthnHandler)        
        // all other should be forbidden
        .addDefaultHandler(ctx -> ctx.fail(401)));
    router.post("/logout").handler(this::logoutHandler);  
    
    String hostURI=buildHostURI();
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
  @SuppressWarnings("deprecation")
  private void authCallback(OAuth2Auth oauth2, String hostURL, RoutingContext context) {
    final String code = context.request().getParam("code");
    // code is a require value
    if (code == null) {
      context.fail(400);
      return;
    }
    final String redirectTo = context.request().getParam("redirect_uri");
    final String redirectURI = hostURL + context.currentRoute().getPath() + "?redirect_uri=" + redirectTo;

    oauth2.authenticate(new JsonObject().put("code", code).put("redirect_uri", redirectURI), ar -> {
      if (ar.failed()) {
        logger.warn("Auth fail");
        context.fail(ar.cause());
      } else {
        logger.info("Auth success");
        context.setUser(ar.result());
        context.response()
          .putHeader("Location", redirectTo)
          .setStatusCode(302)
          .end();
      }
    });
  }

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

  private void loginEntryHandler(OAuth2Auth oauth2, RoutingContext context) {
    context.response()
      .putHeader("Location", generateAuthRedirectURI(oauth2,buildHostURI()))
      .setStatusCode(302)
      .end();
  }

  private void logoutHandler(RoutingContext context) {
    context.clearUser();
    context.session().destroy();
    context.response().setStatusCode(204).end();
  }
  
  @SuppressWarnings("deprecation")
  private void loginKeycloak(RoutingContext ctx) {
    OAuth2Options options = new OAuth2Options()
        .setClientId("")
        .setClientSecret("")
        .setSite("https://keycloak:8080/auth/realms/master");    
    KeycloakAuth.discover(vertx, options).onComplete(result -> {
      JsonObject requestBody = ctx.getBodyAsJson();
      String username = requestBody.getString("username");
      String password = requestBody.getString("password");
  
      JsonObject keycloakRequest = new JsonObject()
          .put("client_id", "")
          .put("client_secret", "")
          .put("grant_type", "password")
          .put("username", username)
          .put("password", password);
      OAuth2Auth oauth2 = result.result();
      oauth2.authenticate(keycloakRequest, ar -> {
        if (ar.failed()) {
            logger.warn("Auth fail");
            ctx.fail(ar.cause());
        } else {
            logger.info("Auth success");
            // Get the access token
            String accessToken = ar.result().principal().getString("access_token");
            // Use the access token to get user info
            WebClient webClient = WebClient.create(vertx);
            webClient.getAbs("https://keycloak:8080/auth/realms/your-realm/protocol/openid-connect/userinfo")
                .putHeader("Authorization", "Bearer " + accessToken)
                .send(userInfoResponse -> {
                    if (userInfoResponse.failed()) {
                        logger.warn("Failed to get user info");
                        ctx.fail(userInfoResponse.cause());
                    } else {
                        JsonObject userInfo = userInfoResponse.result().bodyAsJsonObject();
                        logger.info("User info: " + userInfo.encodePrettily());
                        // Here you can save user info to a database or perform other actions
                        // Set user context
                        ctx.setUser(ar.result());
                        // Redirect or respond with user info
                        ctx.response()
                            .putHeader("Content-Type", "application/json")
                            .end(userInfo.encodePrettily());
                    }
                });
        }
      });
    });
  }

  @SuppressWarnings("deprecation")
  private String generateAuthRedirectURI(OAuth2Auth oauth2,String from) {
    return oauth2.authorizeURL(new JsonObject()
      .put("redirect_uri", from + "/callback?redirect_uri=" + from)
      .put("scope", "")
      .put("state", ""));
  }

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
