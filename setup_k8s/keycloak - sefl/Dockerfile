FROM quay.io/keycloak/keycloak:latest as builder

# Enable health and metrics support
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

WORKDIR /opt/keycloak
# for demonstration purposes only, please make sure to use proper certificates in production instead
RUN keytool -genkeypair -storepass secret -storetype PKCS12 -keyalg RSA -keysize 2048 -dname "CN=server" -alias server -ext "SAN:c=DNS:keycloak" -keystore conf/server.keystore
RUN /opt/keycloak/bin/kc.sh build
RUN keytool -exportcert -alias server -file conf/server.crt -keystore conf/server.keystore -storepass secret -rfc


FROM registry.access.redhat.com/ubi9 AS ubi-micro-build
COPY --from=builder /opt/keycloak/conf/server.crt /etc/pki/ca-trust/source/anchors/server.crt
RUN update-ca-trust
RUN echo "y" | yum install java-11-openjdk-devel
RUN echo "yes" | keytool -importcert -file /etc/pki/ca-trust/source/anchors/server.crt -alias server -keystore /etc/pki/ca-trust/extracted/java/cacerts -storepass changeit

FROM quay.io/keycloak/keycloak:latest
COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY --from=ubi-micro-build /etc/pki /etc/pki
RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]