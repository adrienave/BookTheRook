package com.github.adrienave.booktherook;

public class GameRecord {
    private final String name;

    public GameRecord(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
