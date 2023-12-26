FROM debian:bullseye-slim

WORKDIR /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        g++ \
        cmake \
        libsnappy-dev \
        zlib1g-dev \
        libbz2-dev \
        liblz4-dev \
        libgflags-dev \
        libpthread-stubs0-dev \
        librocksdb-dev \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*


COPY ./rocksdb_test.cpp /app/

RUN g++ -std=c++17 -o rocksdb_test rocksdb_test.cpp -lrocksdb -lpthread -lsnappy -lgflags -lz -lbz2 -llz4

CMD ["./rocksdb_test"]
