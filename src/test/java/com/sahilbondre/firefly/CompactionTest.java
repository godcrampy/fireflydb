package com.sahilbondre.firefly;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.sahilbondre.firefly.TestUtils.deleteFolderAndFilesIfExists;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompactionTest {

    private static final String TEST_FOLDER = "src/test/resources/test_folder_compaction";
    private static final String TEST_LOG_FILE_1 = "1.log";
    private static final String TEST_LOG_FILE_2 = "2.log";
    private static final String TEST_LOG_FILE_3 = "3.log";

    private FireflyDB fireflyDB;

    @BeforeEach
    void setUp() throws IOException {
        deleteFolderAndFilesIfExists(TEST_FOLDER);
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
    }

    @Test
    void givenMultipleLogFiles_whenCompaction_thenAllFilesProcessedCorrectly() throws IOException {
        // Given
        // A FireflyDB instance with a folder path
        fireflyDB.start();

        // When
        // Compaction is triggered
        fireflyDB.compaction();

        // Then
        // All log files are processed correctly
        assertTrue(Files.exists(Paths.get(TEST_FOLDER, "_1.log")));
        assertTrue(Files.exists(Paths.get(TEST_FOLDER, "_2.log")));
        assertTrue(Files.exists(Paths.get(TEST_FOLDER, "_3.log")));
    }
}
