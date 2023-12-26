#include <iostream>
#include <cstdlib>
#include <ctime>
#include <chrono>
#include <numeric>
#include <string>
#include <vector>

#include "rocksdb/db.h"

const std::string db_path = "/tmp/rockstestdb/";

const int ITERATIONS = std::atoi(std::getenv("ITERATIONS"));
const int KEY_LENGTH = std::atoi(std::getenv("KEY_LENGTH"));
const int VALUE_LENGTH = std::atoi(std::getenv("VALUE_LENGTH"));

std::string get_random_bytes_of_length(int length) {
    std::string random_bytes;
    for (int i = 0; i < length; ++i) {
        random_bytes.push_back(static_cast<char>(std::rand() % 256));
    }
    return random_bytes;
}

double save_kv_pair_and_get_time_ms(rocksdb::DB* db, const std::string& key, const std::string& value) {
    auto start_time = std::chrono::high_resolution_clock::now();
    rocksdb::WriteOptions write_options;
    write_options.sync = true;
    rocksdb::Status status = db->Put(write_options, key, value);
    auto end_time = std::chrono::high_resolution_clock::now();
    if (!status.ok()) {
        std::cerr << "Error during write: " << status.ToString() << std::endl;
    }
    return std::chrono::duration_cast<std::chrono::microseconds>(end_time - start_time).count();
}

double get_kv_pair_and_get_time_ms(rocksdb::DB* db, const std::string& key) {
    auto start_time = std::chrono::high_resolution_clock::now();
    std::string value;
    rocksdb::Status status = db->Get(rocksdb::ReadOptions(), key, &value);
    auto end_time = std::chrono::high_resolution_clock::now();
    if (!status.ok()) {
        std::cerr << "Error during read: " << status.ToString() << std::endl;
    }
    return std::chrono::duration_cast<std::chrono::microseconds>(end_time - start_time).count();
}

int main() {
    auto start_time = std::chrono::high_resolution_clock::now();

    std::cout << "Starting RocksDB benchmark..." << std::endl;
    std::cout << "Iterations: " << ITERATIONS << std::endl;
    std::cout << "Key Length: " << KEY_LENGTH << std::endl;
    std::cout << "Value Length: " << VALUE_LENGTH << std::endl;

    // Open RocksDB
    rocksdb::DB* db;
    rocksdb::Options options;
    options.create_if_missing = true;
    rocksdb::Status status = rocksdb::DB::Open(options, db_path, &db);
    if (!status.ok()) {
        std::cerr << "Error opening RocksDB: " << status.ToString() << std::endl;
        return 1;
    }

    // Seed the random number generator
    std::srand(std::time(0));

    // Benchmark writes
    std::cout << "Starting writes..." << std::endl;
    std::vector<std::string> keys;
    std::vector<double> write_times;

    for (int i = 0; i < ITERATIONS; ++i) {
        std::string key = get_random_bytes_of_length(KEY_LENGTH);
        std::string value = get_random_bytes_of_length(VALUE_LENGTH);
        keys.push_back(key);
        write_times.push_back(save_kv_pair_and_get_time_ms(db, key, value));
    }

    std::cout << "\nWrite Test Results:" << std::endl;
    std::cout << "  Average write latency: " << (std::accumulate(write_times.begin(), write_times.end(), 0.0) / write_times.size()) << " mus" << std::endl;
    std::sort(write_times.begin(), write_times.end());
    std::cout << "  P90 write latency: " << write_times[static_cast<int>(write_times.size() * 0.9)] << " mus" << std::endl;

    // Benchmark reads
    std::cout << "\nStarting reads..." << std::endl;
    std::vector<double> read_times;

    for (int i = 0; i < ITERATIONS; ++i) {
        std::string key = keys[std::rand() % keys.size()];
        read_times.push_back(get_kv_pair_and_get_time_ms(db, key));
    }

    std::cout << "\nRead Test Results:" << std::endl;
    std::cout << "  Average read latency: " << (std::accumulate(read_times.begin(), read_times.end(), 0.0) / read_times.size()) << " mus" << std::endl;
    std::sort(read_times.begin(), read_times.end());
    std::cout << "  P90 read latency: " << read_times[static_cast<int>(read_times.size() * 0.9)] << " mus" << std::endl;

    // Benchmark reads and writes
    std::cout << "\nStarting reads and writes..." << std::endl;
    std::vector<double> combined_read_times;
    std::vector<double> combined_write_times;

    for (int i = 0; i < ITERATIONS; ++i) {
        // Write
        std::string key = get_random_bytes_of_length(KEY_LENGTH);
        std::string value = get_random_bytes_of_length(VALUE_LENGTH);
        combined_write_times.push_back(save_kv_pair_and_get_time_ms(db, key, value));
        keys.push_back(key);

        // Read
        key = keys[std::rand() % keys.size()];
        combined_read_times.push_back(get_kv_pair_and_get_time_ms(db, key));
    }

    // Print combined read and write benchmark results
    std::cout << "\nRead and Write Test Results:" << std::endl;
    std::cout << "  Average write latency: " << (std::accumulate(combined_write_times.begin(), combined_write_times.end(), 0.0) / combined_write_times.size()) << " mus" << std::endl;
    std::cout << "  Average read latency: " << (std::accumulate(combined_read_times.begin(), combined_read_times.end(), 0.0) / combined_read_times.size()) << " mus" << std::endl;
    std::sort(combined_write_times.begin(), combined_write_times.end());
    std::sort(combined_read_times.begin(), combined_read_times.end());
    std::cout << "  P90 write latency: " << combined_write_times[static_cast<int>(combined_write_times.size() * 0.9)] << " mus" << std::endl;
    std::cout << "  P90 read latency: " << combined_read_times[static_cast<int>(combined_read_times.size() * 0.9)] << " mus" << std::endl;

    // Close RocksDB
    delete db;

    auto end_time = std::chrono::high_resolution_clock::now();
    std::cout << "\nTotal time: " << std::chrono::duration_cast<std::chrono::seconds>(end_time - start_time).count() << " seconds" << std::endl;

    return 0;
}
