package com.smartconsultor.cluster; 

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.impl.OAuth2API;
import io.vertx.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.cluster.infinispan.ClusterHealthCheck;

import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CSPHandler;
import io.vertx.ext.web.handler.CSRFHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.HSTSHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.XFrameHandler;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.infinispan.InfinispanSessionStore;
import io.vertx.micrometer.PrometheusScrapingHandler;
//import io.vertx.ext.web.sstore.redis.RedisSessionStore;
//import io.vertx.redis.client.Redis;
//import io.vertx.redis.client.RedisAPI;
//import io.vertx.redis.client.RedisOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.PemTrustOptions;

@SuppressWarnings("deprecation")
public class GatewayVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(GatewayVerticle.class); 
  private OAuth2Auth keycloakAuthProvider;
  
  // tag::start[]
  @Override
  public void start() {
    Router router = Router.router(vertx);   
    setupRouter(router);
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(config().getJsonObject("server").getInteger("api.gateway.http.port"))
      .onSuccess(server -> {
        logger.info("Gateway Server started and listening on port {}", server.actualPort()); 
    });
  }
  // end::start[]
  // tag::router[]
  private void setupRouter(Router router) { 
        
    // get configuration
    String host = config().getJsonObject("server").getString("api.gateway.http.address");
    int port = config().getJsonObject("server").getInteger("api.gateway.http.port");
    String baseUrl = String.format("https://%s:%d", host, port);
    
    // body handler
    router.route().handler(BodyHandler.create());  

    // Store session information on the server side
    /* 
    String redisConnectionString= "redis://"+ config().getJsonObject("redis").getString("host") 
                                      + ":" + config().getJsonObject("redis").getString("port");
    RedisOptions options = new RedisOptions()
        .setConnectionString(redisConnectionString)
        .setPassword(config().getJsonObject("redis").getString("password"));
    Redis redisClient = Redis.createClient(vertx, options);
    RedisSessionStore redisSessionStore = RedisSessionStore.create(vertx, redisClient);    
    router.route().handler(SessionHandler.create(redisSessionStore));
    */
    JsonObject storeOptions = new JsonObject()
      .put("servers", new JsonArray()
        .add(new JsonObject()
          .put("host",config().getJsonObject("infinispan").getString("host"))
          .put("port", config().getJsonObject("infinispan").getInteger("port"))
          //.put("username", System.getenv("INFINISPAN_USERNAME"))
          //.put("password", System.getenv("INFINISPAN_PASSWORD"))
          .put("username", config().getJsonObject("infinispan").getString("username"))
          .put("password", config().getJsonObject("infinispan").getString("password"))          
        )
      );
    SessionStore store = InfinispanSessionStore.create(vertx, storeOptions); 
    router.route().handler(SessionHandler.create(store)
                          .setCookieHttpOnlyFlag(true)
                          .setCookieSecureFlag(true)
                          );  

    
    // CSRF handler setup required for logout form
    String csrfSecret = generateCsrfSecret();
    CSRFHandler csrfHandler = CSRFHandler.create(vertx,csrfSecret);
    //router.route().handler(csrfHandler);
    //router.routeWithRegex("^(?!/csp-report-endpoint|/eventbus|/logout).*$").handler(csrfHandler);
    router.routeWithRegex("^(?!/csp-report-endpoint).*$").handler(csrfHandler);
    
        
    // HSTS Handler
    router.route().handler(HSTSHandler.create());

    // CSP handler
    router.route().handler(CSPHandler.create()
      .addDirective("default-src", "auth.smartconsultor.com")
      .addDirective("default-src", "'unsafe-inline'")
      .addDirective("default-src", "'unsafe-eval'")
      .addDirective("report-uri", "/csp-report-endpoint")
    );
    
    // Endpoint để nhận báo cáo CSP
    router.post("/csp-report-endpoint").handler(this::handleCspReport);  

    // XFrame handler
    router.route().handler(XFrameHandler.create(XFrameHandler.DENY));

    // Cors Handler
    enableCorsSupport(router); 
    // add security header
    addSecurityHeaders(router); 
    // static content
    router.route("/*").handler(StaticHandler.create().setCachingEnabled(true));
    
    // errorHandler
    router.route().failureHandler(ErrorHandler.create(vertx)); 

    // create a oauth2 handler for Keycloak
    // Tạo PemTrustOptions từ chứng chỉ CA
    PemTrustOptions trustOptions = new PemTrustOptions().addCertPath("ca.crt");
    // tạo client option
    HttpClientOptions httpClientOptions = new HttpClientOptions()
        .setSsl(true)
        .setTrustOptions(trustOptions);    

    keycloakAuthProvider = KeycloakAuth.create(vertx,OAuth2FlowType.AUTH_CODE, config().getJsonObject("keycloak"), httpClientOptions);
    OAuth2AuthHandler keycloakOAuth2 = OAuth2AuthHandler
        .create(vertx, keycloakAuthProvider,baseUrl+"/callback")
        .setupCallback(router.route("/callback"));

    // protect "/api/*" by keycloakOAuth2
    router.route("/api/*").handler(keycloakOAuth2);    
    // check active
    router.get("/health").handler(rc -> rc.response().end("OK"));
    Handler<Promise<Status>> procedure = ClusterHealthCheck.createProcedure(vertx, false);
    HealthChecks checks = HealthChecks.create(vertx).register("cluster-health", procedure);
    router.get("/readiness").handler(HealthCheckHandler.createWithHealthChecks(checks));

    // websocket
    router.route("/eventbus*").handler(keycloakOAuth2);            
    // Allow events for the designated addresses in/out of the event bus bridge
    SockJSBridgeOptions opts = new SockJSBridgeOptions()
      .addInboundPermitted(new PermittedOptions().setAddress("chat.to.server"))
      .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client"));

    // Create the event bus bridge and add it to the router.
    SockJSHandler ebHandler = SockJSHandler.create(vertx);
    router.route("/eventbus*").subRouter(ebHandler.bridge(opts));
    
    EventBus eb = vertx.eventBus();

    // Register to listen for messages coming IN to the server
    eb.consumer("chat.to.server").handler(message -> {
      // Create a timestamp string
      String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date.from(Instant.now()));
      // Send the message back out to all clients with the timestamp prepended.
      eb.publish("chat.to.client", timestamp + ": " + message.body());
    });          
    // protect "/login" and redirect to home page after successful authentication
    router.route("/login").handler(keycloakOAuth2).handler(ctx -> {
      ctx.redirect("/"); // redirect to your desired URL after successful authentication
    });
        // Đường dẫn để xuất khẩu các chỉ số Prometheus
    router.route("/metrics").handler(PrometheusScrapingHandler.create());
    // logout
    router.post("/logout").handler(this::logoutHandler);    
    // api test
    router.get("/api/*").handler(this::dispatchRequests);  

  }
  // end::router[]

  // tag::dispatchRequests[]
  private void dispatchRequests(RoutingContext rc) {
    int initialOffset = 5; // length of `/api/`
    String path = rc.request().uri();
    if (path.length() <= initialOffset) {
      notFound(rc);
      return;
    }
    logger.info(path);
    String prefix = (path.substring(initialOffset)
    .split("/"))[0];
    logger.info(prefix);
    // generate new relative path
    String newPath = path.substring(initialOffset + prefix.length());
    logger.info(newPath);

    vertx.eventBus().<String>request("greetings", rc.queryParams().get("name"))
      .map(Message::body)
      .onSuccess(reply -> {
        logger.info("Received reply from EventBus: {}", reply);
        rc.response().end(reply);
      })
      .onFailure(error -> {
        logger.error("Failed to receive reply from EventBus", error);
        rc.fail(error);
      }); 
  } 
  // end::dispatchRequests[]

  // CSRF code
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
          logger.error(e.getMessage());
          return null;
      }
  } 
  // CORS
  private void enableCorsSupport(Router router) {
    Set<String> allowHeaders = new HashSet<>();
    allowHeaders.add("x-requested-with");
    allowHeaders.add("Access-Control-Allow-Origin");
    allowHeaders.add("Access-Control-Allow-Method");
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
  private void addSecurityHeaders(Router router) {
    router.route().handler(ctx -> { 
      ctx.response()
        .putHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload")
        .putHeader("X-Content-Type-Options", "nosniff")
        .putHeader("Referrer-Policy", "no-referrer-when-downgrade")
        .putHeader("Feature-Policy", "geolocation 'self'; microphone 'none'; camera 'none'")
        .putHeader("Permissions-Policy", "geolocation=(self), microphone=()");
      ctx.next();
    });
  }  
  // Phương thức xử lý báo cáo CSP
  private void handleCspReport(RoutingContext rc) {
    // Extract the report body
    JsonObject reportJson = rc.getBodyAsJson();
    // Optionally, you can log or process the CSP report
    logger.info("Received CSP report: " + reportJson.encodePrettily());
    // Respond with a success status
    rc.response()
      .setStatusCode(200)
      .end();
  }  
  // logout
  private void logoutHandler(RoutingContext context) {
    User user = context.user();
    if (user == null || user.principal() == null) {
        context.response().setStatusCode(401).end("Unauthorized");
        return;
    }

    String accessToken = user.principal().getString("access_token");
    String refreshToken = user.principal().getString("refresh_token");

    if (accessToken == null || refreshToken == null) {
        context.response().setStatusCode(400).end("Invalid tokens");
        return;
    }    
    // Tạo PemTrustOptions từ chứng chỉ CA
    PemTrustOptions trustOptions = new PemTrustOptions().addCertPath("ca.crt");
    // tạo client option
    HttpClientOptions httpClientOptions = new HttpClientOptions()
        .setSsl(true)
        .setTrustOptions(trustOptions);    

    JsonObject configKeycloak=config().getJsonObject("keycloak");
    OAuth2Options options = new OAuth2Options()
    .setClientId(configKeycloak.getString("resource"))
    .setClientSecret(configKeycloak.getJsonObject("credentials").getString("secret"))
    .setSite(configKeycloak.getString("auth-server-url")) 
    .setLogoutPath("/realms/" + configKeycloak.getString("realm") + "/protocol/openid-connect/logout")
    .setHttpClientOptions(httpClientOptions);
    
    OAuth2API oauth2API = new OAuth2API(vertx,options);
    oauth2API.logout(accessToken, refreshToken)
      .onSuccess(v -> {
        context.clearUser();
        context.session().destroy();
        context.response().setStatusCode(204).end();
      })
      .onFailure(err -> {
        // Log error
        logger.error(err.getMessage());
        context.response().setStatusCode(500).end(); 
      });
  }
  // not found
  private void notFound(RoutingContext context) {
    context.response().setStatusCode(404)
      .putHeader("content-type", "application/json")
      .end(new JsonObject().put("message", "not_found").encodePrettily());
  }
}
