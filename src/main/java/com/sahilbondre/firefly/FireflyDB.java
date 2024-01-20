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
import java.util.HashMap;
import java.util.Map;

public class FireflyDB {
    private static final Map<String, FireflyDB> instances = new HashMap<>();
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
            // Create file-table if it doesn't exist
            Path path = Paths.get(fileTablePath);

            if (Files.exists(path)) {
                this.fileTable = SerializedPersistableFileTable.fromFile(fileTablePath);
            } else {
                this.fileTable = SerializedPersistableFileTable.fromEmpty();
            }

            // Find all files ending with .log
            Files.walkFileTree(Paths.get(folderPath), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    String fileName = file.getFileName().toString();
                    if (fileName.endsWith(".log")) {
                        String fileNameWithoutExtension = fileName.substring(0, fileName.length() - 4);
                        if (isNumeric(fileNameWithoutExtension)) {
                            // Create a RandomAccessLog for each file
                            RandomAccessLog log = new FileChannelRandomAccessLog(file.toString());
                            // Add it to the logMap
                            logMap.put(Integer.parseInt(fileNameWithoutExtension), log);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            // File with the largest number is the active log
            int max = logMap.keySet().stream().max(Integer::compareTo).orElse(0);

            if (!logMap.containsKey(max)) {
                logMap.put(max, new FileChannelRandomAccessLog(folderPath + "/" + max + ".log"));
            }

            activeLog = logMap.get(max);

            // handle the case when there are no logs
            logMap.put(max, activeLog);
        }
        isStarted = true;
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
            throw new IllegalStateException("FireflyDB is not started.");
        }

        // Append to active log
        Segment segment = Segment.fromKeyValuePair(key, value);
        FilePointer filePointer = activeLog.append(segment.getBytes());
        fileTable.put(key, filePointer);
    }

    public byte[] get(byte[] key) throws IOException {
        if (!isStarted) {
            throw new IllegalStateException("FireflyDB is not started.");
        }

        // Get file-pointer from file-table
        FilePointer filePointer = fileTable.get(key);
        if (filePointer == null) {
            throw new IllegalArgumentException("Key not found.");
        }

        // Read from log
        Segment segment = activeLog.readSegment(filePointer.getOffset());
        return segment.getValue();
    }
}
