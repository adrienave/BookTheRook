package com.github.adrienave.booktherook.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemManager {

    public void initializeDataDirectory() throws IOException {
        Path dataFolder = Path.of("./data");
        if (!Files.exists(dataFolder)) {
            Files.createDirectory(dataFolder);
        }
    }
}
