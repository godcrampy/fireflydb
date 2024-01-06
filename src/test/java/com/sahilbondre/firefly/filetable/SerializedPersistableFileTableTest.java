package com.sahilbondre.firefly.filetable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SerializedPersistableFileTableTest {

    private static final String TEST_FILE_PATH = "src/test/resources/map";
    private SerializedPersistableFileTable fileTable;

    @BeforeEach
    void setUp() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_FILE_PATH));
        fileTable = SerializedPersistableFileTable.fromEmpty();
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_FILE_PATH));
    }

    @Test
    void given_KeyValue_When_PuttingAndGet_Then_RetrievedValueMatches() {
        // Given
        byte[] key = "testKey".getBytes();
        FilePointer expectedValue = new FilePointer("test.txt", 42);

        // When
        fileTable.put(key, new FilePointer("test.txt", 42));
        FilePointer retrievedValue = fileTable.get(key);

        // Then
        assertEquals(expectedValue, retrievedValue);
    }

    @Test
    void given_NullKey_When_PuttingAndGet_Then_RetrievedValueIsNull() {
        // Given
        FilePointer value = new FilePointer("test.txt", 42);

        // When
        fileTable.put(null, value);
        FilePointer retrievedValue = fileTable.get(null);

        // Then
        assertNull(retrievedValue);
    }

    @Test
    void given_NullValue_When_PuttingAndGet_Then_RetrievedValueIsNull() {
        // Given
        byte[] key = "testKey".getBytes();

        // When
        fileTable.put(key, null);
        FilePointer retrievedValue = fileTable.get(key);

        // Then
        assertNull(retrievedValue);
    }

    @Test
    void given_KeyValue_When_SavingToDiskAndLoadingFromFile_Then_RetrievedValueMatches() throws FileNotFoundException {
        // Given
        byte[] key = "testKey".getBytes();
        FilePointer value = new FilePointer("test.txt", 42);

        // When
        fileTable.put(key, value);
        fileTable.saveToDisk(TEST_FILE_PATH);
        SerializedPersistableFileTable loadedFileTable = SerializedPersistableFileTable.fromFile(TEST_FILE_PATH);
        FilePointer retrievedValue = loadedFileTable.get(key);

        // Then
        assertEquals(value, retrievedValue);
    }

    @Test
    void given_NonexistentFile_When_LoadingFromFile_Then_FileNotFoundExceptionIsThrown() {
        // When
        // Then
        assertThrows(FileNotFoundException.class,
            () -> SerializedPersistableFileTable.fromFile(TEST_FILE_PATH));
    }

    @Test
    void given_CorruptedFile_When_LoadingFromFile_Then_InvalidFileTableExceptionIsThrown() throws IOException {
        // Given
        // Create a corrupted file by writing invalid data
        Path filePath = Paths.get(TEST_FILE_PATH);
        Files.write(filePath, List.of("Invalid Data"));

        // Then
        assertThrows(InvalidFileTableException.class,
            () -> SerializedPersistableFileTable.fromFile(TEST_FILE_PATH));
    }
}
