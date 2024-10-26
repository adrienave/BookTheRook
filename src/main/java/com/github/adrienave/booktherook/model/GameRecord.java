package com.github.adrienave.booktherook.model;


import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class GameRecord {
    private final String name;
    @Builder.Default
    private List<HalfMove> moves = new ArrayList<>();
    @Builder.Default
    private int currentMoveIndex = -1;
    private String location;

    @Override
    public String toString() {
        return this.name;
    }

    public String toFormattedString() {
        StringBuilder gameText = new StringBuilder(this.getName() + "\n");
        int currentMoveIndex = 1;
        boolean isWhiteMove = true;
        for (HalfMove move : this.getMoves()) {
            if (isWhiteMove) {
                gameText.append(String.format("%d. %s", currentMoveIndex, move.getAlgebraicNotation()));
            } else {
                gameText.append(String.format(" %s \n", move.getAlgebraicNotation()));
                currentMoveIndex++;
            }
            isWhiteMove = !isWhiteMove;
        }
        return gameText.toString();
    }
}
