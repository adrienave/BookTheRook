package com.github.adrienave.booktherook.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemManager {
    private static final Path DATA_PATH = Path.of("./data");

    public void initializeDataDirectory() throws IOException {
        if (!Files.exists(DATA_PATH)) {
            Files.createDirectory(DATA_PATH);
        }
    }

    public void createFolder(String name) throws IOException {
        Files.createDirectory(DATA_PATH.resolve(Path.of(name)));
    }
}
