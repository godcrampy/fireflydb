package com.sahilbondre.firefly.log;

import com.sahilbondre.firefly.filetable.FilePointer;
import com.sahilbondre.firefly.model.Segment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
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
        try {
            randomAccessLog.close();
        } catch (ClosedChannelException e) {
            // Ignore
        }
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

    @Test
    void givenLogWithContent_whenReadSegment_thenReturnsCorrectSegment() throws IOException, InvalidRangeException {
        // Given
        // A log with existing content
        Segment firstSegment = Segment.fromKeyValuePair("Hello".getBytes(), "World".getBytes());
        Segment secondSegment = Segment.fromKeyValuePair("Foo".getBytes(), "Bar".getBytes());
        FilePointer firstFilePointer = randomAccessLog.append(firstSegment.getBytes());
        FilePointer secondFilePointer = randomAccessLog.append(secondSegment.getBytes());

        // When
        Segment firstReadSegment = randomAccessLog.readSegment(firstFilePointer.getOffset());
        Segment secondReadSegment = randomAccessLog.readSegment(secondFilePointer.getOffset());

        // Then
        assertArrayEquals(firstSegment.getBytes(), firstReadSegment.getBytes());
        assertArrayEquals(secondSegment.getBytes(), secondReadSegment.getBytes());
        assertEquals("Hello", new String(firstReadSegment.getKey()));
        assertEquals("World", new String(firstReadSegment.getValue()));
        assertEquals("Foo", new String(secondReadSegment.getKey()));
        assertEquals("Bar", new String(secondReadSegment.getValue()));
    }

    @Test
    void givenLogWithContent_whenReadSegmentWithInvalidOffset_thenThrowsInvalidRangeException() throws IOException {
        // Given
        // A log with existing content
        Segment firstSegment = Segment.fromKeyValuePair("Hello".getBytes(), "World".getBytes());
        Segment secondSegment = Segment.fromKeyValuePair("Foo".getBytes(), "Bar".getBytes());
        randomAccessLog.append(firstSegment.getBytes());
        randomAccessLog.append(secondSegment.getBytes());

        // When/Then
        assertThrows(InvalidRangeException.class, () -> randomAccessLog.readSegment(-1));
        assertThrows(InvalidRangeException.class, () -> randomAccessLog.readSegment(100));
    }

    @Test
    void givenEmptyLog_whenReadSegment_thenThrowsInvalidRangeException() {
        // Given
        // An empty log

        // When/Then
        assertThrows(InvalidRangeException.class, () -> randomAccessLog.readSegment(0));
    }

    @Test
    void givenLogWithContent_whenAppend_thenReturnsCorrectFilePointer() throws IOException {
        // Given
        // A log with existing content

        // When
        FilePointer fp1 = randomAccessLog.append("Hello".getBytes());
        FilePointer fp2 = randomAccessLog.append("World".getBytes());

        // Then
        assertEquals(TEST_FILE_NAME, fp1.getFileName());
        assertEquals(0, fp1.getOffset());
        assertEquals(TEST_FILE_NAME, fp2.getFileName());
        assertEquals(5, fp2.getOffset());
    }
}
