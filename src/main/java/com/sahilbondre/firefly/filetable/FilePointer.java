package com.sahilbondre.firefly.filetable;

import java.util.Objects;

public class FilePointer {
    private String fileName;
    private long offset;

    public FilePointer(String fileName, long offset) {
        this.fileName = fileName;
        this.offset = offset;
    }

    public FilePointer() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilePointer that = (FilePointer) o;
        return offset == that.offset && fileName.equals(that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, offset);
    }
}
