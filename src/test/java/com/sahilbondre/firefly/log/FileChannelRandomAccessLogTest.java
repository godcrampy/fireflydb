package com.sahilbondre.firefly.log;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class FileChannelRandomAccessLogTest {

    private static final String TEST_FILE_NAME = "src/test/resources/test.log";
    private static final Path TEST_FILE_PATH = Paths.get(TEST_FILE_NAME);
    private FileChannelRandomAccessLog randomAccessLog;

    @BeforeEach
    void setUp() throws IOException {
        Files.deleteIfExists(TEST_FILE_PATH);
        Files.createFile(TEST_FILE_PATH);
        randomAccessLog = new FileChannelRandomAccessLog(TEST_FILE_NAME);
    }

    @AfterEach
    void tearDown() throws IOException {
        randomAccessLog.close();
        Files.deleteIfExists(TEST_FILE_PATH);
    }

    @Test
    void givenEmptyLog_whenGetSize_thenReturnsZero() throws IOException {
        // Given
        // An empty log

        // When
        long size = randomAccessLog.size();

        // Then
        assertEquals(0, size);
    }


    @Test
    void givenLogWithContent_whenGetSize_thenReturnsCorrectSize() throws IOException {
        // Given
        // A log with content

        // When
        randomAccessLog.append("Hello".getBytes());
        randomAccessLog.append("World".getBytes());

        // Then
        assertEquals(10, randomAccessLog.size());
    }

    @Test
    void givenLog_whenGetFilePath_thenReturnsCorrectPath() {
        // Given
        // A log instance

        // When
        String filePath = randomAccessLog.getFilePath();

        // Then
        assertEquals(TEST_FILE_NAME, filePath);
    }

    @Test
    void givenLogWithContent_whenAppend_thenAppendsCorrectly() throws IOException {
        // Given
        // A log with existing content

        // When
        randomAccessLog.append("Hello".getBytes());
        randomAccessLog.append("World".getBytes());
        byte[] result = randomAccessLog.read(0, randomAccessLog.size());

        // Then
        assertArrayEquals("HelloWorld".getBytes(), result);
    }

    @Test
    void givenLogWithContent_whenReadSubset_thenReturnsSubset() throws IOException, InvalidRangeException {
        // Given
        // A log with existing content

        // When
        randomAccessLog.append("The quick brown fox".getBytes());
        byte[] result = randomAccessLog.read(4, 5);

        // Then
        assertArrayEquals("quick".getBytes(), result);
    }

    @Test
    void givenInvalidRange_whenRead_thenThrowsInvalidRangeException() throws IOException {
        // Given
        randomAccessLog.append("Hello".getBytes());
        // An invalid range for reading

        // When/Then
        assertThrows(InvalidRangeException.class, () -> randomAccessLog.read(0, -1));
        assertThrows(InvalidRangeException.class, () -> randomAccessLog.read(-1, 5));
        assertThrows(InvalidRangeException.class, () -> randomAccessLog.read(15, 10));
        assertThrows(InvalidRangeException.class, () -> randomAccessLog.read(2, 10));
        assertThrows(InvalidRangeException.class, () -> randomAccessLog.read(0, 6));
    }

    @Test
    void givenLog_whenClose_thenFileIsNotAccessible() throws IOException {
        // Given
        // An open log

        // When
        randomAccessLog.close();

        // Then
        assertTrue(Files.exists(TEST_FILE_PATH));
        assertThrows(IOException.class, () -> randomAccessLog.append("NewContent".getBytes()));
    }
}
