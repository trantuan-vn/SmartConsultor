package com.smartconsultor.cluster;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.ServerConfigurationBuilder;
import io.vertx.core.json.JsonObject;

public class InfinispanRemoteManager extends RemoteCacheManager {

    public InfinispanRemoteManager(JsonObject config) {
        super(createConfiguration(config));
    }

    private static Configuration createConfiguration(JsonObject config) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder
            .connectionTimeout(config.getJsonObject("infinispan").getInteger("connectionTimeout"))
            .socketTimeout(config.getJsonObject("infinispan").getInteger("socketTimeout"));
        ServerConfigurationBuilder builderServer=builder.addServer();
        
        builderServer
            .host(config.getJsonObject("infinispan").getString("host"))
            .port(config.getJsonObject("infinispan").getInteger("port"));
        builderServer.security().authentication()
            .username(config.getJsonObject("infinispan").getString("username"))
            .password(config.getJsonObject("infinispan").getString("password"))
            .realm( "default")
            .saslMechanism( "DIGEST-MD5");

        return builder.build();
    }
}