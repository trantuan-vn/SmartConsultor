# Sử dụng image debian với tag buster (hoặc bất kỳ phiên bản bạn muốn)
FROM debian:latest

# Cài đặt các gói phụ thuộc cần thiết
RUN apt-get update && \
	apt-get install -y sed && \
	apt-get install -y curl && \	
	apt-get install -y jq

COPY openjdk-20_linux-x64_bin.tar.gz /opt/java/openjdk.tar.gz
RUN mkdir -p /opt/java/openjdk-20 && \
	tar xz --strip-components=1 -C /opt/java/openjdk-20 -f /opt/java/openjdk.tar.gz && \
    rm /opt/java/openjdk.tar.gz

ENV JAVA_HOME=/opt/java/openjdk-20
ENV PATH=$PATH:$JAVA_HOME/bin


# Tạo thư mục Kafka và chuyển đến đó
WORKDIR /opt

# Sao chép file kafka_2.13-3.5.0.tgz vào thư mục opt
COPY kafka_2.13-3.5.0.tgz ./


RUN tar -xzf kafka_2.13-3.5.0.tgz && \
    rm -f kafka_2.13-3.5.0.tgz

# Chuyển đến thư mục Kafka mới được giải nén
WORKDIR /opt/kafka_2.13-3.5.0

COPY controller.properties config/kraft/controller.properties

COPY broker.properties config/kraft/broker.properties


CMD TOKEN=$(cat /var/run/secrets/kubernetes.io/serviceaccount/token) && \
	CONTRLOLER_REPLICAS=$(curl --cacert /var/run/secrets/kubernetes.io/serviceaccount/ca.crt -H "Authorization: Bearer $TOKEN" https://kubernetes/apis/apps/v1/namespaces/default/statefulsets | jq -r '.items[] | select(.metadata.name == "controller") | .spec.replicas') && \
	VOTERS="1@controller-0:9093" && \
    for i in $(seq 2 $CONTRLOLER_REPLICAS); do \
        VOTERS="${VOTERS}, ${i}@controller-$((i-1)):9093"; \
    done && \
    if [ "$KAFKA_ROLE" = "controller" ]; then \
        KAFKA_ID=$(( $(echo $(uname -n) | grep -o '[0-9]\+') + 1 )); \
        sed -i "s/\${KAFKA_ID}/$KAFKA_ID/g" config/kraft/controller.properties && \
        sed -i "s/\${VOTERS}/$VOTERS/g" config/kraft/controller.properties && \
        bin/kafka-storage.sh format -t "$KAFKA_CLUSTER_ID" -c config/kraft/controller.properties && \
        bin/kafka-server-start.sh config/kraft/controller.properties; \
    elif [ "$KAFKA_ROLE" = "broker" ]; then \
        KAFKA_ID=$(( 10 + $(echo $(uname -n) | grep -o '[0-9]\+') + 1 )); \
        sed -i "s/\${KAFKA_ID}/$KAFKA_ID/g" config/kraft/broker.properties && \
        sed -i "s/\${VOTERS}/$VOTERS/g" config/kraft/broker.properties && \
        bin/kafka-storage.sh format -t "$KAFKA_CLUSTER_ID" -c config/kraft/broker.properties && \
        bin/kafka-server-start.sh config/kraft/broker.properties; \
    else \
        exit 0; \
    fi
