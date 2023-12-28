package com.sahilbondre.firefly;

import java.util.HashMap;
import java.util.Map;

public class FireflyDB {
    private static final Map<String, FireflyDB> instances = new HashMap<>();
    private final String folderPath;

    private FireflyDB(String folderPath) {
        this.folderPath = folderPath;
    }

    public static synchronized FireflyDB getInstance(String folderPath) {
        instances.computeIfAbsent(folderPath, FireflyDB::new);
        return instances.get(folderPath);
    }

    public String getFolderPath() {
        return folderPath;
    }
}
