package com.github.adrienave.booktherook.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class GameRecord {
    private final String name;
    private List<String> moves = new ArrayList<>();

    @Override
    public String toString() {
        return this.name;
    }
}
