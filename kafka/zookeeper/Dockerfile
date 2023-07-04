# Sử dụng image debian với tag buster (hoặc bất kỳ phiên bản bạn muốn)
FROM debian:latest

# Cài đặt các gói phụ thuộc cần thiết
RUN apt-get update 

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

# Sao chép file zookeeper.properties vào thư mục config của Kafka
COPY zookeeper.properties ./config/

# Khởi động ZooKeeper
CMD ["bin/zookeeper-server-start.sh", "config/zookeeper.properties"]
