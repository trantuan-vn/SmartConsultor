package io.vertx.howtos.cluster;

import io.vertx.core.*;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.cluster.infinispan.ClusterHealthCheck;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;

import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
//import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.handler.TimeoutHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(BackendVerticle.class);

  // tag::config[]
  private static final int HTTP_PORT = Integer.parseInt(System.getenv().getOrDefault("HTTP_PORT", "0"));
  private static final String POD_NAME = System.getenv().getOrDefault("POD_NAME", "unknown");
  // end::config[]
  
  // tag::start[]
  @Override
  public void start() {
    // set vertx logger delegate factory to slf4j
    String logFactory = System.getProperty("org.vertx.logger-delegate-factory-class-name");
    if (logFactory == null) {
        System.setProperty("org.vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
    } 

    registerConsumer();

    Router router = setupRouter();

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(HTTP_PORT)
      .onSuccess(server -> log.info("Backend Server started and listening on port {}", server.actualPort()));
  }
  // end::start[]

  // tag::consumer[]
  private void registerConsumer() {
    vertx.eventBus().<String>consumer("greetings", msg -> {
      log.info("Body {}", msg.body());
      msg.reply(String.format("Hello %s from %s", msg.body(), POD_NAME));
    });
  }
  // end::consumer[]

  // tag::router[]
  private Router setupRouter() {
    Router router = Router.router(vertx);
    // set router options
    router.route().handler(BodyHandler.create().setBodyLimit(10 * 1024 * 1024)); // 10MB max body size
    router.route().handler(ResponseTimeHandler.create()); // add a response header: x-response-time: xyzms
    router.route().handler(TimeoutHandler.create(500)); // request timeout in ms
    //router.route().failureHandler(ErrorHandler.create(false)); // no exception details

    // use customized request logger
    // there are three logger format: DEFAULT, SHORT, TINY, see Slf4jRequestLogger.java for details
    // you can make it configurable, e.g. dev using DEFAULT, prod using TINY
    LoggerFormat loggerFormat = LoggerFormat.DEFAULT;
    router.route().handler(RequestLogHandler.create(loggerFormat));

    router.get("/health").handler(rc -> rc.response().end("OK"));

    Handler<Promise<Status>> procedure = ClusterHealthCheck.createProcedure(vertx, false);
    HealthChecks checks = HealthChecks.create(vertx).register("cluster-health", procedure);
    router.get("/readiness").handler(HealthCheckHandler.createWithHealthChecks(checks));
    return router;
  }
  // end::router[]

  // tag::main[]
  public static void main(String[] args) {

    ClusterManager mgr = new InfinispanClusterManager();
       
    Vertx.builder()
      .withClusterManager(mgr) 
      .buildClustered().onComplete(res -> {
        if (res.succeeded()) {
          Vertx vertx = res.result();
          vertx.deployVerticle(new BackendVerticle());
        } else {
          res.cause().printStackTrace();
        }
    });    
  }
  // end::main[]
}
