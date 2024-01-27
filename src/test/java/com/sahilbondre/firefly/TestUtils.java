package com.sahilbondre.firefly;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class TestUtils {
    public static void deleteFolderAndFilesIfExists(String folderPath) throws IOException {
        // check if folder exists
        Path path = Paths.get(folderPath);
        if (Files.exists(path)) {
            // Delete the all the files in the test folder
            Stream<Path>
                files = Files.walk(path);
            files
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException ignored) {
                    }
                });

        }
    }
}
