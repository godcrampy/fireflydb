FROM python:3.10-slim

# Install required system dependencies
RUN apt-get update \
    && apt-get install -y libleveldb-dev build-essential \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

RUN pip install plyvel

WORKDIR /app

COPY leveldb.py /app/

CMD ["python", "leveldb.py"]
