#!/bin/bash

# Lấy giá trị scale từ tham số dòng lệnh
SCALE=$1

# Mở tệp docker-compose.yml để ghi dữ liệu
echo "version: \"3\"
services:" > docker-compose.yml

# Thêm các block Redis và redis-trib theo scale
for i in $(seq 1 $SCALE); do
  echo "
  redis${i}:
    image: redis:latest
    command: redis-server --port 700${i} --cluster-enabled yes --cluster-config-file nodes.conf --cluster-node-timeout 5000 --appendonly yes
    volumes:
      - ./redis${i}:/data
    ports:
      - \"700${i}:700${i}\"" >> docker-compose.yml
done

HOSTPORT=""
for i in $(seq 1 $SCALE); do
    HOSTPORT="${HOSTPORT} redis${i}:700${i}"
done


echo "
	redis-trib:
    image: redis:latest
    command: echo \"yes\" | redis-cli --cluster create ${HOSTPORT} --cluster-replicas 1" >> docker-compose.yml
      
      
echo "depends_on:" >> docker-compose.yml

for i in $(seq 1 $SCALE); do
  echo "    - redis${i}" >> docker-compose.yml
done

# Chạy docker-compose
docker-compose up -d

# Thông báo hoàn thành
echo "Redis cluster đã được triển khai với scale là $SCALE"
