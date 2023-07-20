FROM postgres:latest

# Cài đặt pkg-config và libpq-dev để xây dựng pgpool
RUN apt-get update && apt-get install -y --no-install-recommends \
    pkg-config \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

# Sinh vô một định danh cho pgpool
ENV PGPOOL_VERSION=pgpool-II-4.1.5
ENV PGPOOL_DIR=/usr/src/$PGPOOL_VERSION

# Tải xuống và cài đặt pgpool từ mã nguồn
RUN set -eux; \
    mkdir -p $PGPOOL_DIR; \
    curl -L https://www.pgpool.net/mediawiki/images/$PGPOOL_VERSION.tar.gz | tar zx -C $PGPOOL_DIR --strip-components=1; \
    cd $PGPOOL_DIR; \
    ./configure; \
    make; \
    make install

# Sao chép tệp cấu hình mặc định của pgpool
COPY pgpool.conf /usr/local/etc/pgpool.conf

# Thông báo cổng mà pgpool lắng nghe
EXPOSE 5432

# Chạy lệnh pgpool khi container được khởi chạy
CMD ["pgpool", "-n", "-f", "/usr/local/etc/pgpool.conf"]
