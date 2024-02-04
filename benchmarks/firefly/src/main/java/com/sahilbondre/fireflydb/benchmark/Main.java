package com.sahilbondre.fireflydb.benchmark;

import com.sahilbondre.firefly.FireflyDB;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class Main {
    private static final String TEST_FOLDER = "src/test/resources/test_folder";
    private static final int ITERATIONS = 100000;
    private static final int KEY_LENGTH = 8;
    private static final int VALUE_LENGTH = 100;
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private static final FireflyDB fireflyDB = FireflyDB.getInstance(TEST_FOLDER);



    public static void main(String[] args) throws IOException {
        Files.createDirectories(Paths.get(TEST_FOLDER));

        fireflyDB.start();

        logger.info("Starting benchmark...");
        logger.info("Iterations: " + ITERATIONS);
        logger.info("Key length: " + KEY_LENGTH);
        logger.info("Value length: " + VALUE_LENGTH);

        logger.info("Starting writes...");

        long[] writeTimes = new long[ITERATIONS];

        List<byte[]> availableKeys = new ArrayList<>();

        long startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            byte[] key = getRandomBytes(KEY_LENGTH);
            byte[] value = getRandomBytes(VALUE_LENGTH);

            long writeTime = saveKeyValuePairAndGetTime(key, value);
            availableKeys.add(key);
            writeTimes[i] = writeTime;
        }
        long totalTime = (System.nanoTime() - startTime) / 1000; // Convert nanoseconds to microseconds
        logger.info("Total time for writes: " + totalTime + " mus");

        double averageWriteTime = 0;
        for (long writeTime : writeTimes) {
            averageWriteTime += writeTime;
        }
        averageWriteTime /= ITERATIONS;

        logger.info("Average write latency: " + averageWriteTime + " mus");

        // Calculate p90 write latency
        // Sort write times
        Arrays.sort(writeTimes);
        long p90WriteTime = writeTimes[(int) (ITERATIONS * 0.9)];
        logger.info("p90 write latency: " + p90WriteTime + " mus");

        // Benchmark reads
        logger.info("\nStarting reads...");

        long[] readTimes = new long[ITERATIONS];

        startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            // Get a random key from the list of available keys
            byte[] key = availableKeys.get(new Random().nextInt(availableKeys.size()));

            long readTime = getKeyValuePairAndGetTime(key);
            readTimes[i] = readTime;
        }
        totalTime = (System.nanoTime() - startTime) / 1000; // Convert nanoseconds to microseconds
        logger.info("Total time for reads: " + totalTime + " mus");

        double averageReadTime = 0;
        for (long readTime : readTimes) {
            averageReadTime += readTime;
        }
        averageReadTime /= ITERATIONS;

        logger.info("Average read latency: " + averageReadTime + " mus");

        // Calculate p90 read latency
        // Sort read times
        Arrays.sort(readTimes);
        long p90ReadTime = readTimes[(int) (ITERATIONS * 0.9)];
        logger.info("p90 read latency: " + p90ReadTime + " mus");


        // Benchmark reads and writes
        logger.info("\nStarting reads and writes...");


        startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            byte[] writeKey = getRandomBytes(KEY_LENGTH);
            byte[] writeValue = getRandomBytes(VALUE_LENGTH);

            long writeTime = saveKeyValuePairAndGetTime(writeKey, writeValue);

            availableKeys.add(writeKey);

            byte[] readKey = availableKeys.get(new Random().nextInt(availableKeys.size()));

            long readTime = getKeyValuePairAndGetTime(readKey);

            writeTimes[i] = writeTime;
            readTimes[i] = readTime;
        }
        totalTime = (System.nanoTime() - startTime) / 1000; // Convert nanoseconds to microseconds
        logger.info("Total time for reads and writes: " + totalTime + " mus");

        averageReadTime = 0;
        for (long readTime : readTimes) {
            averageReadTime += readTime;
        }
        averageReadTime /= ITERATIONS;

        averageWriteTime = 0;
        for (long writeTime : writeTimes) {
            averageWriteTime += writeTime;
        }
        averageWriteTime /= ITERATIONS;

        logger.info("Average read latency: " + averageReadTime + " mus");
        logger.info("Average write latency: " + averageWriteTime + " mus");

        // Calculate p90 read latency
        // Sort read times
        Arrays.sort(readTimes);

        // Calculate p90 write latency
        // Sort write times
        Arrays.sort(writeTimes);

        p90ReadTime = readTimes[(int) (ITERATIONS * 0.9)];
        logger.info("p90 read latency: " + p90ReadTime + " mus");

        p90WriteTime = writeTimes[(int) (ITERATIONS * 0.9)];
        logger.info("p90 write latency: " + p90WriteTime + " mus");
    }

    private static byte[] getRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new Random().nextBytes(bytes);
        return bytes;
    }

    private static long saveKeyValuePairAndGetTime(byte[] key, byte[] value) throws IOException {
        long startTime = System.nanoTime();
        fireflyDB.set(key, value);
        return (System.nanoTime() - startTime) / 1000; // Convert nanoseconds to microseconds
    }

    private static long getKeyValuePairAndGetTime(byte[] key) throws IOException {
        long startTime = System.nanoTime();
        fireflyDB.get(key);
        return (System.nanoTime() - startTime) / 1000; // Convert nanoseconds to microseconds
    }
}
