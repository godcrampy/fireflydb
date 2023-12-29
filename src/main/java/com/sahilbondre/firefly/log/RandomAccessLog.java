package com.sahilbondre.firefly.log;

import java.io.IOException;

public interface RandomAccessLog {
    long size() throws IOException;

    String getFilePath();

    void append(byte[] message) throws IOException;

    byte[] read(long offset, long length) throws IOException, InvalidRangeException;

    void close() throws IOException;
}
