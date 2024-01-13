package com.sahilbondre.firefly;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FireflyDBTest {

    private static final String TEST_FOLDER = "src/test/resources/test_folder";
    private static final String TEST_LOG_FILE_1 = "1.log";
    private static final String TEST_LOG_FILE_2 = "2.log";
    private static final String TEST_LOG_FILE_3 = "3.log";

    private FireflyDB fireflyDB;

    @BeforeEach
    void setUp() throws IOException {
        // Create a test folder and log files
        Files.createDirectories(Paths.get(TEST_FOLDER));
        Files.createFile(Paths.get(TEST_FOLDER, TEST_LOG_FILE_1));
        Files.createFile(Paths.get(TEST_FOLDER, TEST_LOG_FILE_2));
        Files.createFile(Paths.get(TEST_FOLDER, TEST_LOG_FILE_3));

        fireflyDB = FireflyDB.getInstance(TEST_FOLDER);
    }

    @AfterEach
    void tearDown() throws IOException {
        fireflyDB.stop();
        // Cleanup: Delete the test folder and its contents
        try (Stream<Path> pathStream = Files.walk(Paths.get(TEST_FOLDER))) {
            pathStream
                .sorted((path1, path2) -> -path1.compareTo(path2))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void givenFolderPath_whenStarted_thenInstanceCreatedAndMarkedAsStarted() throws IOException {
        // Given
        // A FireflyDB instance with a folder path

        // When
        fireflyDB.start();

        // Then
        assertNotNull(fireflyDB);
        assertEquals(TEST_FOLDER, fireflyDB.getFolderPath());
        assertTrue(fireflyDB.isStarted());
    }

    @Test
    void givenStartedInstance_whenStop_thenLogsClosed() throws IOException {
        // Given
        // A started FireflyDB instance
        fireflyDB.start();
        assertTrue(fireflyDB.isStarted());

        // When
        fireflyDB.stop();

        // Then
        assertFalse(fireflyDB.isStarted());
    }

    @Test
    void givenStartedInstance_whenSetAndGet_thenValuesAreCorrect() throws IOException {

        // Given
        fireflyDB.start();
        assertTrue(fireflyDB.isStarted());

        // Set a value
        byte[] key = "testKey".getBytes();
        byte[] value = "testValue".getBytes();
        fireflyDB.set(key, value);

        // Get the value
        byte[] retrievedValue = fireflyDB.get(key);
        assertArrayEquals(value, retrievedValue);
    }

    @Test
    void givenUnstartedInstance_whenSet_thenExceptionThrown() {
        // Given
        byte[] key = "testKey".getBytes();
        byte[] value = "testValue".getBytes();

        // When/Then
        // Attempt to set a value without starting the instance
        assertThrows(IllegalStateException.class, () -> fireflyDB.set(key, value));
    }

    @Test
    void givenUnstartedInstance_whenGet_thenExceptionThrown() {
        // Given
        byte[] key = "testKey".getBytes();

        // When/Then
        // Attempt to get a value without starting the instance
        assertThrows(IllegalStateException.class, () -> fireflyDB.get(key));
    }

    @Test
    void givenNonexistentKey_whenGet_thenExceptionThrown() throws IOException {
        // Given
        fireflyDB.start();
        assertTrue(fireflyDB.isStarted());
        byte[] key = "nonexistentKey".getBytes();

        // When/Then
        // Attempt to get a nonexistent key
        assertThrows(IllegalArgumentException.class, () -> fireflyDB.get(key));
    }
}
