package com.sahilbondre.firefly;

import com.sahilbondre.firefly.log.FileChannelRandomAccessLog;
import com.sahilbondre.firefly.log.RandomAccessLog;
import com.sahilbondre.firefly.model.Segment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.sahilbondre.firefly.TestUtils.deleteFolderContentsIfExists;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompactionTest {

    private static final String TEST_FOLDER = "src/test/resources/test_folder_compaction";
    private static final String TEST_LOG_FILE_1 = "1.log";
    private static final String TEST_LOG_FILE_2 = "2.log";
    private static final String TEST_LOG_FILE_3 = "3.log";

    private FireflyDB fireflyDB;

    @BeforeEach
    void setUp() throws IOException {
        deleteFolderContentsIfExists(TEST_FOLDER);
        // Create a test folder and log files
        Files.createDirectories(Paths.get(TEST_FOLDER));
        Files.createFile(Paths.get(TEST_FOLDER, TEST_LOG_FILE_1));
        Files.createFile(Paths.get(TEST_FOLDER, TEST_LOG_FILE_2));
        Files.createFile(Paths.get(TEST_FOLDER, TEST_LOG_FILE_3));

        RandomAccessLog log1 = new FileChannelRandomAccessLog(TEST_FOLDER + "/" + TEST_LOG_FILE_1);
        RandomAccessLog log2 = new FileChannelRandomAccessLog(TEST_FOLDER + "/" + TEST_LOG_FILE_2);
        RandomAccessLog log3 = new FileChannelRandomAccessLog(TEST_FOLDER + "/" + TEST_LOG_FILE_3);

        log1.append(Segment.fromKeyValuePair("key1".getBytes(), "value1".getBytes()).getBytes());
        log1.append(Segment.fromKeyValuePair("key2".getBytes(), "value2".getBytes()).getBytes());
        log1.append(Segment.fromKeyValuePair("key3".getBytes(), "value3".getBytes()).getBytes());

        log2.append(Segment.fromKeyValuePair("key4".getBytes(), "value4".getBytes()).getBytes());
        log2.append(Segment.fromKeyValuePair("key1".getBytes(), "value5".getBytes()).getBytes());
        log2.append(Segment.fromKeyValuePair("key2".getBytes(), "value6".getBytes()).getBytes());

        log3.append(Segment.fromKeyValuePair("key7".getBytes(), "value7".getBytes()).getBytes());
        log3.append(Segment.fromKeyValuePair("key8".getBytes(), "value8".getBytes()).getBytes());
        log3.append(Segment.fromKeyValuePair("key1".getBytes(), "value9".getBytes()).getBytes());

        log1.close();
        log2.close();
        log3.close();

        fireflyDB = FireflyDB.getInstance(TEST_FOLDER);
    }

    @AfterEach
    void tearDown() throws IOException {
        fireflyDB.stop();
        deleteFolderContentsIfExists(TEST_FOLDER);
    }

    @Test
    void givenMultipleLogFiles_whenCompaction_thenAllFilesRenamedCorrectly() throws IOException {
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
        assertEquals("value9", new String(fireflyDB.get("key1".getBytes())));
        assertEquals("value6", new String(fireflyDB.get("key2".getBytes())));
        assertEquals("value3", new String(fireflyDB.get("key3".getBytes())));
        assertEquals("value4", new String(fireflyDB.get("key4".getBytes())));
        assertEquals("value7", new String(fireflyDB.get("key7".getBytes())));
        assertEquals("value8", new String(fireflyDB.get("key8".getBytes())));
    }
}
