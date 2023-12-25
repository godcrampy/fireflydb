# Use an official Erlang runtime as a parent image
FROM erlang:26-slim

RUN apt-get update \
    && apt-get install -y git build-essential \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

RUN git clone "https://github.com/basho/bitcask.git" \
    && cd bitcask \
    && make

WORKDIR /app/bitcask

COPY bitcask_test.erl /app/bitcask/
COPY run.sh /app/bitcask/

RUN erlc bitcask_test.erl

RUN ["chmod", "+x", "./run.sh"]

CMD "/app/bitcask/run.sh"
