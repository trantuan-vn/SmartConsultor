# Sử dụng image debian với tag buster (hoặc bất kỳ phiên bản bạn muốn)
FROM debian:latest

# Cài đặt các gói phụ thuộc cần thiết
RUN apt-get update 

# Tạo thư mục Kafka và chuyển đến đó
WORKDIR /opt

# Sao chép file kafka_2.13-3.0.0.tgz vào thư mục opt
COPY kafka_2.13-3.0.0.tgz ./


RUN tar -xzf kafka_2.13-3.0.0.tgz && \
    rm -f kafka_2.13-3.0.0.tgz

# Chuyển đến thư mục Kafka mới được giải nén
WORKDIR /opt/kafka_2.13-3.0.0

# Sao chép file zookeeper.properties vào thư mục config của Kafka
COPY server.properties ./config/

# Khởi động Kafka
CMD ["bin/kafka-server-start.sh", "config/server.properties"]