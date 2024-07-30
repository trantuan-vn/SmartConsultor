package com.smartconsultor.cluster;

import io.vertx.core.Launcher;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;

@SuppressWarnings("deprecation")
public class GatewayLauncher extends Launcher {
  private static final Logger logger = LoggerFactory.getLogger(GatewayVerticle.class);  
  @Override
  public void beforeStartingVertx(VertxOptions options) {
    logger.info("GatewayLauncher.beforeStartingVertx: running");
    options.setMetricsOptions(
      new MicrometerMetricsOptions()
        .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
        .setEnabled(true)
    ); 
  }
  public static void main(String[] args) {
    new GatewayLauncher().dispatch(args);
  }
}