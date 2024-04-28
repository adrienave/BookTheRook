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
    private List<HalfMove> moves = new ArrayList<>();
    private int currentMoveIndex;
    private String location;

    public GameRecord(String name, List<HalfMove> moves) {
        this.name = name;
        this.moves = moves;
        this.currentMoveIndex = -1;
        this.location = null;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
