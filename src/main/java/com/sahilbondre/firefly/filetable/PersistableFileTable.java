package com.sahilbondre.firefly.filetable;

import java.io.FileNotFoundException;

public interface PersistableFileTable {
    void put(byte[] key, FilePointer value);

    FilePointer get(byte[] key);

    void saveToDisk(String filePath) throws FileNotFoundException;
}
