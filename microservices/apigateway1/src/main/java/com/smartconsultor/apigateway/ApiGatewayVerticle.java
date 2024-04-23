package com.smartconsultor.apigateway;

import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.cluster.infinispan.ClusterHealthCheck;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import io.vertx.core.spi.cluster.ClusterManager;

import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;

public class ApiGatewayVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(ApiGatewayVerticle.class);
    @Override
    public void start() {
        ConfigStoreOptions store = new ConfigStoreOptions()
            .setType("configmap")
            .setConfig(new JsonObject()
                .put("namespace", "default")
                .put("name", "apigateway"));
    
        ConfigRetriever retriever = ConfigRetriever.create(vertx,
            new ConfigRetrieverOptions().addStore(store));

        retriever.getConfig(ar -> {
            if (ar.succeeded()) {
                JsonObject config = ar.result();
                Integer serverPort = config.getJsonObject("app.properties").getInteger("server.port");                
                Router router = Router.router(vertx);
                setupRouter(router);
                vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(serverPort)
                    .onSuccess(server -> log.info("ApiGateway Server started and listening on port {}", server.actualPort()));
            } else {
                log.error("Failed to retrieve configuration: " + ar.cause().getMessage());
            }
        });

    }

    private void setupRouter(Router router) {
        //router.get("/hello").handler(this::handleHelloRequest);
        router.get("/health").handler(rc -> rc.response().end("OK"));
        Handler<Promise<Status>> procedure = ClusterHealthCheck.createProcedure(vertx, false);
        HealthChecks checks = HealthChecks.create(vertx).register("cluster-health", procedure);
        router.get("/readiness").handler(HealthCheckHandler.createWithHealthChecks(checks));
    }  

    public static void main(String[] args) {
        ClusterManager mgr = new InfinispanClusterManager();
        
        Vertx.builder()
          .withClusterManager(mgr)
          .buildClustered().onComplete(res -> {
            if (res.succeeded()) {
              Vertx vertx = res.result();
              vertx.deployVerticle(new ApiGatewayVerticle());
            } else {
              res.cause().printStackTrace();
            }
        });
    }    
}
