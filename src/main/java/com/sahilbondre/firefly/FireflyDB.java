package com.sahilbondre.firefly;

import com.sahilbondre.firefly.filetable.FilePointer;
import com.sahilbondre.firefly.filetable.PersistableFileTable;
import com.sahilbondre.firefly.filetable.SerializedPersistableFileTable;
import com.sahilbondre.firefly.log.FileChannelRandomAccessLog;
import com.sahilbondre.firefly.log.RandomAccessLog;
import com.sahilbondre.firefly.model.Segment;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FireflyDB {
    private static final Map<String, FireflyDB> instances = new HashMap<>();
    private static final String NOT_STARTED_ERROR_MESSAGE = "FireflyDB is not started.";
    // 4 GB
    private static final long MAX_LOG_SIZE = 4 * 1024 * 1024 * 1024L;

    private final String folderPath;

    private final String fileTablePath;
    private final Map<Integer, RandomAccessLog> logMap = new HashMap<>();
    private RandomAccessLog activeLog;
    private boolean isStarted = false;
    private PersistableFileTable fileTable;

    private FireflyDB(String folderPath) {
        this.folderPath = folderPath;
        this.fileTablePath = folderPath + "/map.kryo";
    }

    public static synchronized FireflyDB getInstance(String folderPath) {
        instances.computeIfAbsent(folderPath, FireflyDB::new);
        return instances.get(folderPath);
    }

    private static boolean isNumeric(String str) {
        return str.matches("\\d+");
    }

    public String getFolderPath() {
        return folderPath;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public synchronized void start() throws IOException {
        if (!isStarted) {
            isStarted = true;
            compaction();
        }
    }

    public synchronized void stop() throws IOException {
        if (isStarted) {
            // Save file-table to disk
            fileTable.saveToDisk(fileTablePath);
            // Close all RandomAccessLog
            for (RandomAccessLog log : logMap.values()) {
                log.close();
            }
        }
        isStarted = false;
    }

    public synchronized void set(byte[] key, byte[] value) throws IOException {
        if (!isStarted) {
            throw new IllegalStateException(NOT_STARTED_ERROR_MESSAGE);
        }

        // Append to active log
        Segment segment = Segment.fromKeyValuePair(key, value);
        FilePointer filePointer = activeLog.append(segment.getBytes());
        fileTable.put(key, filePointer);

        // Check if compaction is needed
        if (activeLog.size() > MAX_LOG_SIZE) {
            moveToNewActiveLog();
        }
    }

    private void moveToNewActiveLog() throws IOException {
        // Create a new log
        int nextActiveLogId = activeLog == null ? 1 : activeLog.getLogId() + 1;
        RandomAccessLog nextActiveLog = new FileChannelRandomAccessLog(folderPath + "/" + nextActiveLogId + ".log");
        // Update logMap
        logMap.put(nextActiveLogId, nextActiveLog);
        activeLog = nextActiveLog;
    }

    public byte[] get(byte[] key) throws IOException {
        if (!isStarted) {
            throw new IllegalStateException(NOT_STARTED_ERROR_MESSAGE);
        }

        // Get file-pointer from file-table
        FilePointer filePointer = fileTable.get(key);
        if (filePointer == null) {
            throw new IllegalArgumentException("Key not found.");
        }

        // Read from log
        String filename = Paths.get(filePointer.getFileName()).getFileName().toString();
        Integer logId = Integer.parseInt(filename.substring(0, filename.length() - 4));
        RandomAccessLog log = logMap.get(logId);
        Segment segment = log.readSegment(filePointer.getOffset());
        return segment.getValue();
    }

    public synchronized void compaction() throws IOException {
        if (!isStarted) {
            throw new IllegalStateException(NOT_STARTED_ERROR_MESSAGE);
        }

        closeAllLogMapsIfOpen();

        // Iterate over all log files in descending order
        List<RandomAccessLog> logs = getRandomAccessLogsFromDir(folderPath);

        if (!logs.isEmpty()) {
            // Set the last log as active log
            activeLog = logs.get(0);
        }

        this.fileTable = SerializedPersistableFileTable.fromEmpty();

        // Create a new log
        moveToNewActiveLog();
        // Iterate over all logs
        for (RandomAccessLog log : logs) {
            // Iterate over all segments in the log
            long offset = 0;

            while (offset < log.size()) {
                Segment segment = log.readSegment(offset);
                offset += segment.getBytes().length;
                // Append only if the key is not seen before
                if (fileTable.get(segment.getKey()) != null) {
                    continue;
                }
                // Append to new log
                FilePointer filePointer = activeLog.append(segment.getBytes());
                fileTable.put(segment.getKey(), filePointer);
            }

            orphanizeLog(log);
        }

        // update logmap
        logMap.clear();
        logMap.put(activeLog.getLogId(), activeLog);
        // save file-table
        fileTable.saveToDisk(fileTablePath);
    }

    private void closeAllLogMapsIfOpen() {
        for (RandomAccessLog log : logMap.values()) {
            try {
                log.close();
            } catch (IOException ignored)  {
                // Ignore
            }
        }
    }

    private void orphanizeLog(RandomAccessLog log) throws IOException {
        log.close();
        // rename all stale logs and add underscore before file name
        Path oldPath = Paths.get(log.getFilePath());
        Path dir = oldPath.getParent();
        Path newPath = Paths.get(dir.toString(), "_" + oldPath.getFileName().toString());
        Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private List<RandomAccessLog> getRandomAccessLogsFromDir(String dir) throws IOException {
        List<RandomAccessLog> logs = new ArrayList<>();
        Files.walkFileTree(Paths.get(dir), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                String fileName = file.getFileName().toString();
                if (fileName.endsWith(".log")) {
                    String fileNameWithoutExtension = fileName.substring(0, fileName.length() - 4);
                    if (isNumeric(fileNameWithoutExtension)) {
                        // Create a RandomAccessLog for each file
                        RandomAccessLog log = new FileChannelRandomAccessLog(file.toString());
                        // Add it to the logMap
                        logs.add(log);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });


        // Sort the logs in descending order
        logs.sort((o1, o2) -> {
            int o1Id = Integer.parseInt(o1.getFilePath().substring(o1.getFilePath().lastIndexOf("/") + 1, o1.getFilePath().length() - 4));
            int o2Id = Integer.parseInt(o2.getFilePath().substring(o2.getFilePath().lastIndexOf("/") + 1, o2.getFilePath().length() - 4));
            return Integer.compare(o2Id, o1Id);
        });
        return logs;
    }
}
