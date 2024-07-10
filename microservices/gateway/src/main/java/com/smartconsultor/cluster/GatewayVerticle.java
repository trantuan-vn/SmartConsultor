package com.smartconsultor.cluster; 

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
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
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.HSTSHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.XFrameHandler;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.sstore.redis.RedisSessionStore;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

@SuppressWarnings("deprecation")
public class GatewayVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(GatewayVerticle.class);

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
    String baseUrl = String.format("http://%s:%d", host, port);
    String redisConnectionString= "redis://"+ config().getJsonObject("redis").getString("host") 
                                      + ":" + config().getJsonObject("redis").getString("port");

    // body handler
    router.route().handler(BodyHandler.create());

    // Store session information on the server side
    RedisOptions options = new RedisOptions()
        .setConnectionString(redisConnectionString)
        .setPassword(config().getJsonObject("redis").getString("password"));
    Redis redisClient = Redis.createClient(vertx, options);
    RedisSessionStore redisSessionStore = RedisSessionStore.create(vertx, redisClient);    
    router.route().handler(SessionHandler.create(redisSessionStore));

    // CSRF handler setup required for logout form
    //String csrfSecret = generateCsrfSecret();
    //router.route().handler(CSRFHandler.create(vertx,csrfSecret));

    // HSTS Handler
    router.route().handler(HSTSHandler.create());

    // CSP handler
       
    router.route().handler(CSPHandler.create()
      .addDirective("default-src", "'unsafe-inline'")
      .addDirective("default-src", "'unsafe-eval'")
      /* 
      .addDirective("default-src", "*.gstatic.com")
      .addDirective("default-src", "*.googleapis.com")
      .addDirective("default-src", "*.unpkg.io")
      .addDirective("script-src", "'self'")
      .addDirective("script-src", "'unsafe-inline'")
      .addDirective("script-src", "'unsafe-eval'")
      .addDirective("script-src", "*.gstatic.com")
      .addDirective("script-src", "*.googleapis.com")
      .addDirective("script-src", "*.unpkg.io")      
      .addDirective("script-src", "data")      
      */
      .addDirective("report-uri", "/csp-report-endpoint")
    
    );
    
    // Endpoint để nhận báo cáo CSP
    router.post("/csp-report-endpoint").handler(this::handleCspReport);  

    // XFrame handler
    router.route().handler(XFrameHandler.create(XFrameHandler.DENY));

    // Cors Handler
    enableCorsSupport(router);

    // static content
    RedisAPI redisAPI = RedisAPI.api(redisClient);
    router.route("/*").handler(routingContext -> {
      String path = routingContext.normalisedPath();
      // Kiểm tra cache trong Redis
      redisAPI.get(path, redisResult -> {
        if (redisResult.succeeded() && redisResult.result() != null) {
            // Nếu có trong cache, trả về nội dung từ Redis
            logger.info("{} đã tồn tại trong redis", path);
            routingContext.response().end(redisResult.result().toString());
        } else {
            // Nếu không có trong cache, chuyển tiếp yêu cầu đến StaticHandler
            routingContext.next();
            // Lấy nội dung phản hồi
            routingContext.response().bodyEndHandler(buffer -> {
              if (buffer!=null) {
                String body = buffer.toString();
                // Lưu vào Redis 
                redisAPI.setex(path, config().getJsonObject("redis").getString("TTL"), body, redisSetResult -> {
                    if (redisSetResult.failed()) {
                      logger.error(redisSetResult.cause().getMessage());
                    }
                });
              }
            });
        }
      });
    });    
    router.route("/*").handler(StaticHandler.create());
    

    // errorHandler
    router.route().failureHandler(ErrorHandler.create(vertx));

    // create a oauth2 handler for Keycloak
    OAuth2Auth keycloakAuthProvider = KeycloakAuth.create(vertx,OAuth2FlowType.AUTH_CODE,config().getJsonObject("keycloak"));
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
    router.route("/eventbus/*").handler(keycloakOAuth2);            

    SockJSHandlerOptions websocketOptions = new SockJSHandlerOptions()
      .setHeartbeatInterval(3000)
      .setRegisterWriteHandler(true);
    SockJSHandler sockJSHandler = SockJSHandler.create(vertx, websocketOptions);  

    //test
    PermittedOptions inboundPermitted = new PermittedOptions().setAddress("chat.to.server");
    //inboundPermitted.setRequiredAuthority("place_orders");
    PermittedOptions outboundPermitted = new PermittedOptions().setAddress("chat.to.client");
    
    SockJSBridgeOptions sockJSBridgeOptions = new SockJSBridgeOptions()
      .addInboundPermitted(inboundPermitted)
      .addOutboundPermitted(outboundPermitted);

    router.route("/eventbus/*")
    .subRouter(sockJSHandler.bridge(sockJSBridgeOptions, event -> {
        if (event.socket().webUser() == null) {
          // Người dùng chưa đăng nhập, không làm gì cả
          event.complete(false);  // Hoặc bạn có thể gửi một phản hồi lỗi tùy thuộc vào trường hợp sử dụng của bạn
          return;
        }        
        
        String writeHandlerID = event.socket().writeHandlerID();     
        String sessionID = event.socket().webSession().id(); 
        String websocketSessionID = "websocket." + sessionID;

        if (event.type() == BridgeEventType.SOCKET_CREATED) {
          logger.info("SOCKET_CREATED writeHandlerID: {}", websocketSessionID);
          // Store the writeHandlerID in Redis
          redisAPI.setex(websocketSessionID, config().getJsonObject("redis").getString("TTL"), writeHandlerID, redisSetResult -> {
              if (redisSetResult.failed()) {
                  logger.error(redisSetResult.cause().getMessage());
              }
          });
        } else if (event.type() == BridgeEventType.SOCKET_CLOSED) {
            logger.info("SOCKET_CLOSED websocketSessionID: {}", websocketSessionID);
            redisAPI.del(Collections.singletonList(websocketSessionID), redisDelResult -> {
                if (redisDelResult.failed()) {
                    logger.error(redisDelResult.cause().getMessage());
                }
            });
        } else if (event.type() == BridgeEventType.SOCKET_PING) {
          logger.info("SOCKET_PING websocketSessionID: {}", websocketSessionID);
        } else if (event.type() == BridgeEventType.SEND || event.type() == BridgeEventType.PUBLISH) {
          logger.info("{} websocketSessionID: {}",event.type().toString(), websocketSessionID);
        }
    
        event.complete(true);
    }));
    
  

    // test
    router.get("/api/getWriteHandlerID").handler(rc -> {
      // Create a timestamp string
      String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date.from(Instant.now()));
      String username = rc.user().principal().getString("preferred_username");       
      String websocketUsername="websocket."+username;
      redisAPI.get(websocketUsername, redisGetResult -> {
        if (redisGetResult.succeeded()) {
          // If the result is succeeded, pass the result to the resultHandler
          if (redisGetResult.result()!=null){
            vertx.eventBus().send(redisGetResult.result().toString(), Buffer.buffer(timestamp));
          }
        } else {
          logger.error(redisGetResult.cause().getMessage());
          rc.fail(redisGetResult.cause());
        }
      });     

    });

    router.get("/hello").handler(this::handleHelloRequest);    
  }
  // end::router[]

  // tag::handle-request[]
  private void handleHelloRequest(RoutingContext rc) {
    logger.info("Name {}",rc.queryParams().get("name"));
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
          logger.error(res.cause().getMessage());
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
          logger.error(e.getMessage());
          return null;
      }
  } 
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
}
