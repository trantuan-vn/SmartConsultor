package com.smartconsultor.cluster; 

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.providers.FacebookAuth;
import io.vertx.ext.auth.oauth2.providers.GoogleAuth;
import io.vertx.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.ext.cluster.infinispan.ClusterHealthCheck;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import io.vertx.core.spi.cluster.ClusterManager;

import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
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

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

@SuppressWarnings("deprecation")
public class GatewayVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(GatewayVerticle.class);

  // tag::config[]
  private static final int HTTP_PORT = Integer.parseInt(System.getenv().getOrDefault("HTTP_PORT", "8080"));
  // end::config[]

  // tag::start[]
  @Override
  public void start() {
    Router router = Router.router(vertx);   

    setupRouter(router);

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(HTTP_PORT)
      .onSuccess(server -> {
        logger.info("Gateway Server started and listening on port {}", server.actualPort());
      });
  }
  // end::start[]
  // tag::router[]
  private void setupRouter(Router router) {
    
    //router.route().handler(LoggerHandler.create(LoggerFormat.DEFAULT));
    // get HTTP host and port from configuration, or use default value
    String host = config().getString("api.gateway.http.address", "localhost");
    int port = config().getInteger("api.gateway.http.port", HTTP_PORT);
    String baseUrl = String.format("https://%s:%d", host, port);
    String googleClientID=config().getString("google.clientID", "");
    String googleClientSecret=config().getString("google.clientSecret", "");
    String facebookClientID=config().getString("facebook.clientID", "");
    String facebookClientSecret=config().getString("facebook.clientSecret", "");
    logger.info(host);
    router.get("/hello").handler(this::handleHelloRequest);
    // body handler
    router.route().handler(BodyHandler.create());
    // Store session information on the server side
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    // CSRF handler setup required for logout form
    String csrfSecret = generateCsrfSecret();
    router.route().handler(CSRFHandler.create(vertx,csrfSecret));
    // HSTS Handler
    router.route().handler(HSTSHandler.create());
    // CSP handler
    router.route().handler(CSPHandler.create()
      .addDirective("default-src", "'unsafe-inline'")
      .addDirective("default-src", "'unsafe-eval'")
      .addDirective("default-src", "*.gstatic.com")
      .addDirective("default-src", "*.googleapis.com")
      .addDirective("report-uri", "/csp-report-endpoint")
//      .addDirective("script-src", "'self' 'unsafe-inline' https://www.gstatic.com")
//      .addDirective("style-src", "'self' 'unsafe-inline' https://fonts.googleapis.com")
//      .addDirective("img-src", "'self' data:")
//      .addDirective("font-src", "'self' 'unsafe-inline' https://fonts.gstatic.com")
//      .addDirective("connect-src", "'self' 'unsafe-inline' https://api.example.com")
//      .addDirective("object-src", "'none'")
//      .addDirective("media-src", "'self'")
//      .addDirective("frame-src", "'self'")
    );
    // Endpoint để nhận báo cáo CSP
    router.post("/csp-report-endpoint").handler(this::handleCspReport);    
    // XFrame handler
    router.route().handler(XFrameHandler.create(XFrameHandler.DENY));
    // Cors Handler
    enableCorsSupport(router);
    // static content
    //router.route("/static/*").handler(StaticHandler.create());
    //router.route("/*").handler(StaticHandler.create("webroot"));
    router.route("/*").handler(StaticHandler.create());

    // create oath2 instance for google
    OAuth2Auth googleAuthProvider = GoogleAuth.create(vertx,googleClientID,googleClientSecret);
    // create a oauth2 handler 
    OAuth2AuthHandler googleOAuth2 = OAuth2AuthHandler
      .create( vertx, googleAuthProvider, baseUrl+"/google-callback")
      .setupCallback(router.route("/google-callback"));
    
    // create oath2 instance for facebook
    OAuth2Auth facebookAuthProvider = FacebookAuth.create(vertx,facebookClientID,facebookClientSecret);
    // Create an OAuth2 handler for Facebook
    OAuth2AuthHandler facebookOAuth2 = OAuth2AuthHandler
      .create(vertx, facebookAuthProvider, baseUrl+"/facebook-callback")
      .setupCallback(router.route("/facebook-callback"));
    // create oath2 instance for keycloak
    
    OAuth2Auth keycloakAuthProvider = KeycloakAuth.create(vertx,OAuth2FlowType.PASSWORD,config());
    // create a oauth2 handler for Keycloak
    OAuth2AuthHandler keycloakOAuth2 = OAuth2AuthHandler.create(vertx, keycloakAuthProvider);
    // MultiTenantHandler protect /api/*
    router.route("/api/*").handler(
      MultiTenantHandler.create("X-Tenant")
        // tenants using facebook should go this way:
        .addTenantHandler("facebook", facebookOAuth2)
        // tenants using google should go this way:
        .addTenantHandler("google", googleOAuth2)
        // tenants using google should go this way:
        .addTenantHandler("keycloak", keycloakOAuth2)        
        // tenants using webauthn should go this way:
        //.addTenantHandler("webauthn", webAuthnHandler)        
        // all other should be forbidden
        .addDefaultHandler(ctx -> {
          // Redirect to login page
          ctx.response()
            .putHeader("location", "/#/login")
            .setStatusCode(302)
            .end();
        })
    );    

    // Định nghĩa route đăng nhập
    router.get("/login").handler(ctx -> {
      String tenant = ctx.request().getHeader("X-Tenant");
      if (tenant == null ) {
        ctx.fail(400);
        return;
      } else {
        if (tenant != "keycloak"){
          ctx.fail(400);
          return;
        }
      }

      JsonObject params = ctx.getBodyAsJson();

      String username = params.getString("username");
      String password = params.getString("password");

      JsonObject tokenConfig = new JsonObject()
              .put("username", username)
              .put("password", password);

      // Sử dụng oauth2 để xác thực và lấy token
      keycloakAuthProvider.authenticate(tokenConfig, res -> {
          if (res.succeeded()) {
              // Lưu thông tin người dùng vào session
              //ctx.session().put("user", res.result().principal());
              ctx.setUser(res.result());
              ctx.response()
                .putHeader("content-type", "application/json")
                .end(res.result().principal().encode());
          } else {
              ctx.response().setStatusCode(401).end();
          }
      });
    });        
    
    router.get("/api/hello").handler(rc -> rc.response().end("api/Hello"));

    router.get("/health").handler(rc -> rc.response().end("OK"));
    Handler<Promise<Status>> procedure = ClusterHealthCheck.createProcedure(vertx, false);
    HealthChecks checks = HealthChecks.create(vertx).register("cluster-health", procedure);
    router.get("/readiness").handler(HealthCheckHandler.createWithHealthChecks(checks));
  }
  // end::router[]

  // tag::handle-request[]
  private void handleHelloRequest(RoutingContext rc) {
    
    //System.out.println("Name " + rc.queryParams().get("name"));

    logger.info("Name {}",rc.queryParams().get("name"));
    
    vertx.eventBus().<String>request("greetings", rc.queryParams().get("name"))
      .map(Message::body)
      .onSuccess(reply -> {
        logger.info("Received reply from EventBus: {}", reply);
        rc.response().end(reply);
      })
      .onFailure(error -> {
        logger.error("Failed to receive reply from EventBus", error);
        //rc.fail(error);
      }); 
     
  } 
  // end::handle-request[]

  // tag::main[]
  public static void main(String[] args) {
    
    ClusterManager mgr = new InfinispanClusterManager();
        
    Vertx.builder()
      .withClusterManager(mgr)
      .buildClustered().onComplete(res -> {
        if (res.succeeded()) {
          Vertx vertx = res.result();
          vertx.deployVerticle(new GatewayVerticle());
        } else {
          res.cause().printStackTrace();
        }
    });
  }
  // end::main[]
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

  // Phương thức xử lý báo cáo CSP
  private void handleCspReport(RoutingContext rc) {
      HttpServerRequest request=rc.request();
      // Lấy nội dung JSON từ body của request
      request.bodyHandler(buffer -> {
          String jsonBody = buffer.toString();
          // In ra nội dung báo cáo CSP
          logger.info("Received CSP report:");
          logger.info(jsonBody);
          // Trả về HTTP status 204 (No Content) cho client
          HttpServerResponse response = request.response();
          response.setStatusCode(204);
          response.end();
      });
  }  
}
