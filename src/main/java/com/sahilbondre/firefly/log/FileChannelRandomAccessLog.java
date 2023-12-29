package com.sahilbondre.firefly.log;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelRandomAccessLog implements RandomAccessLog {

    private final String filePath;
    private final RandomAccessFile randomAccessFile;
    private final FileChannel fileChannel;

    public FileChannelRandomAccessLog(String filePath) throws IOException {
        this.filePath = filePath;
        this.randomAccessFile = new RandomAccessFile(filePath, "rw");
        this.fileChannel = randomAccessFile.getChannel();
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
    public void append(byte[] message) throws IOException {
        fileChannel.position(fileChannel.size());
        ByteBuffer buffer = ByteBuffer.wrap(message);
        fileChannel.write(buffer);
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

    public void close() throws IOException {
        fileChannel.close();
        randomAccessFile.close();
    }
}