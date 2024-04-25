package com.github.adrienave.booktherook.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public List<String> getFolderNames() throws IOException {
        try (Stream<Path> stream = Files.list(DATA_PATH)) {
            return stream.filter(Files::isDirectory).map(path -> path.getFileName().toString()).collect(Collectors.toList());
        }
    }

    public void createGame(String name, String subFolder) throws IOException {
        Files.createFile(DATA_PATH.resolve(Path.of(subFolder, String.format("%s.pgn", name))));
    }
}
