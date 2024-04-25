package com.github.adrienave.booktherook.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GameRecord {
    private final String name;

    @Override
    public String toString() {
        return this.name;
    }
}
