package com.sahilbondre.firefly.log;

import com.sahilbondre.firefly.filetable.FilePointer;
import com.sahilbondre.firefly.model.Segment;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;

public class FileChannelRandomAccessLog implements RandomAccessLog {

    private final String filePath;
    private final RandomAccessFile randomAccessFile;
    private final FileChannel fileChannel;
    private final FileLock fileLock;

    public FileChannelRandomAccessLog(String filePath) throws IOException {
        this.filePath = filePath;
        this.randomAccessFile = new RandomAccessFile(filePath, "rw");
        this.fileChannel = randomAccessFile.getChannel();
        this.fileLock = fileChannel.lock();
    }

    @Override
    public long size() throws IOException {
        return fileChannel.size();
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public FilePointer append(byte[] message) throws IOException {
        fileChannel.position(fileChannel.size());
        ByteBuffer buffer = ByteBuffer.wrap(message);
        fileChannel.write(buffer);
        return new FilePointer(filePath, fileChannel.size() - message.length);
    }

    @Override
    public byte[] read(long offset, long length) throws IOException, InvalidRangeException {
        long fileSize = fileChannel.size();

        if (offset < 0 || offset >= fileSize || length <= 0 || offset + length > fileSize) {
            throw new InvalidRangeException("Invalid offset or length");
        }

        fileChannel.position(offset);
        ByteBuffer buffer = ByteBuffer.allocate((int) length);
        fileChannel.read(buffer);
        return buffer.array();
    }

    @Override
    public Segment readSegment(long offset) throws IOException, InvalidRangeException {
        long fileSize = fileChannel.size();

        if (offset < 0 || offset >= fileSize) {
            throw new InvalidRangeException("Invalid offset");
        }

        // Read Key Size
        byte[] keySizeBytes = new byte[Segment.KEY_SIZE_LENGTH];
        fileChannel.read(ByteBuffer.wrap(keySizeBytes), offset + Segment.KEY_SIZE_LENGTH);

        // Read Value Size
        byte[] valueSizeBytes = new byte[Segment.VALUE_SIZE_LENGTH];
        fileChannel.read(ByteBuffer.wrap(valueSizeBytes),
            offset + Segment.KEY_SIZE_LENGTH + Segment.VALUE_SIZE_LENGTH);

        // Total Size
        int totalSize = Segment.CRC_LENGTH + Segment.KEY_SIZE_LENGTH +
            Segment.VALUE_SIZE_LENGTH + byteArrayToInt(keySizeBytes) + byteArrayToInt(valueSizeBytes);

        // Read entire segment
        byte[] segmentBytes = new byte[totalSize];
        fileChannel.read(ByteBuffer.wrap(segmentBytes), offset);


        Segment segment = Segment.fromByteArray(segmentBytes);

        // Validate CRC
        if (!segment.isSegmentValid()) {
            throw new InvalidRangeException("Segment is invalid");
        }

        return segment;
    }

    @Override
    public Integer getLogId() {
        String fileNameWithoutPath = Paths.get(filePath).getFileName().toString();
        return Integer.parseInt(fileNameWithoutPath.substring(0, fileNameWithoutPath.length() - 4));
    }

    private int byteArrayToInt(byte[] bytes) {
        return (bytes[0] << 8) | (bytes[1] & 0xFF);
    }

    public void close() throws IOException {
        fileLock.release();
        fileChannel.close();
        randomAccessFile.close();
    }
}
