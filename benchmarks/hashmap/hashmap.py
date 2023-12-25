import os
import random
from datetime import datetime

db = {}
startTime = datetime.now()

# read from environment variables
ITERATIONS = int(os.environ.get("ITERATIONS"))
KEY_LENGTH = int(os.environ.get("KEY_LENGTH"))
VALUE_LENGTH = int(os.environ.get("VALUE_LENGTH"))

print("Starting HashMap benchmark")
print("Iterations: " + str(ITERATIONS))
print("Key length: " + str(KEY_LENGTH))
print("Value length: " + str(VALUE_LENGTH))


def get_random_bytes_of_length(length):
    return os.urandom(length)


def save_kv_pair_and_get_time_mus(k, v):
    start_time = datetime.now()
    db[k] = v
    return (datetime.now() - start_time).microseconds


def get_kv_pair_and_get_time_mus(k):
    start_time = datetime.now()
    db[k]
    return (datetime.now() - start_time).microseconds


# Benchmark writes
print()
print("Starting writes")

times = []
keys = []

for i in range(ITERATIONS):
    key = get_random_bytes_of_length(KEY_LENGTH)
    value = get_random_bytes_of_length(VALUE_LENGTH)
    keys.append(key)
    times.append(save_kv_pair_and_get_time_mus(key, value))

print("Average time to save a key-value pair: " + str(sum(times) / len(times)) + " mus")
print("Total time: " + str(datetime.now() - startTime))
print("P90 latency: " + str(sorted(times)[int(len(times) * 0.9)]) + " mus")

# Benchmark reads
print()
print("Starting reads")
times = []

for i in range(ITERATIONS):
    key = random.choice(keys)

    times.append(get_kv_pair_and_get_time_mus(key))

print("Average time to read a key-value pair: " + str(sum(times) / len(times)) + " mus")
print("Total time: " + str(datetime.now() - startTime))
print("P90 latency: " + str(sorted(times)[int(len(times) * 0.9)]) + " mus")

# Benchmark reads and writes
print()
print("Starting reads and writes")
read_times = []
write_times = []

for i in range(ITERATIONS):
    key = get_random_bytes_of_length(KEY_LENGTH)
    value = get_random_bytes_of_length(VALUE_LENGTH)

    write_times.append(save_kv_pair_and_get_time_mus(key, value))
    keys.append(key)

    key = random.choice(keys)
    read_times.append(get_kv_pair_and_get_time_mus(key))

print("Average time to write a key-value pair: " + str(sum(write_times) / len(write_times)) + " mus")
print("Average time to read a key-value pair: " + str(sum(read_times) / len(read_times)) + " mus")
print("Total time: " + str(datetime.now() - startTime))
print("P90 latency to write: " + str(sorted(write_times)[int(len(write_times) * 0.9)]) + " mus")
print("P90 latency to read: " + str(sorted(read_times)[int(len(read_times) * 0.9)]) + " mus")

print()
print("Time to run: " + str(datetime.now() - startTime))
