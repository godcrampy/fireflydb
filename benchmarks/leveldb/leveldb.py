import os
import random
from datetime import datetime

import plyvel

db = plyvel.DB('/tmp/testdb/', create_if_missing=True)
startTime = datetime.now()

# read from environment variables
ITERATIONS = int(os.environ.get("ITERATIONS"))
KEY_LENGTH = int(os.environ.get("KEY_LENGTH"))
VALUE_LENGTH = int(os.environ.get("VALUE_LENGTH"))

print("Starting LevelDB benchmark")
print("Iterations: " + str(ITERATIONS))
print("Key length: " + str(KEY_LENGTH))
print("Value length: " + str(VALUE_LENGTH))


def get_random_bytes_of_length(length):
    return os.urandom(length)


def save_kv_pair_and_get_time_mus(k, v):
    start_time = datetime.now()
    db.put(k, v, sync=True)
    return (datetime.now() - start_time).microseconds


def get_kv_pair_and_get_time_mus(k):
    start_time = datetime.now()
    db.get(k)
    return (datetime.now() - start_time).microseconds


# Benchmark writes
print("\nStarting writes...")

times = []
keys = []

for i in range(ITERATIONS):
    key = get_random_bytes_of_length(KEY_LENGTH)
    value = get_random_bytes_of_length(VALUE_LENGTH)
    keys.append(key)
    times.append(save_kv_pair_and_get_time_mus(key, value))

print("\nWrite Test Results:")
print("  Average write latency: " + str(sum(times) / len(times)) + " mus")
print("  P90 write latency: " + str(sorted(times)[int(len(times) * 0.9)]) + " mus")
print("  Total time: " + str(datetime.now() - startTime))

# Benchmark reads
print("\nStarting reads...")
times = []

for i in range(ITERATIONS):
    key = random.choice(keys)

    times.append(get_kv_pair_and_get_time_mus(key))

print("\nRead Test Results:")
print("  Average read latency: " + str(sum(times) / len(times)) + " mus")
print("  P90 read latency: " + str(sorted(times)[int(len(times) * 0.9)]) + " mus")
print("  Total time: " + str(datetime.now() - startTime))

# Benchmark reads and writes
print("\nStarting reads and writes...")
read_times = []
write_times = []

for i in range(ITERATIONS):
    key = get_random_bytes_of_length(KEY_LENGTH)
    value = get_random_bytes_of_length(VALUE_LENGTH)

    write_times.append(save_kv_pair_and_get_time_mus(key, value))
    keys.append(key)

    key = random.choice(keys)
    read_times.append(get_kv_pair_and_get_time_mus(key))

print("\nRead and Write Test Results:")
print("  Average write latency: " + str(sum(write_times) / len(write_times)) + " mus")
print("  Average read latency: " + str(sum(read_times) / len(read_times)) + " mus")
print("  P90 write latency: " + str(sorted(write_times)[int(len(write_times) * 0.9)]) + " mus")
print("  P90 read latency: " + str(sorted(read_times)[int(len(read_times) * 0.9)]) + " mus")
print("  Total time: " + str(datetime.now() - startTime))

print("\n Total time to run tests: " + str(datetime.now() - startTime))
