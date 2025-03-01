package com.github.adrienave.booktherook.model;


import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class GameRecord {
    private final String name;
    private final String whitePlayerName;
    private final String blackPlayerName;
    private final String result;
    private final String parsingError;
    @Builder.Default
    private List<HalfMove> moves = new ArrayList<>();
    @Builder.Default
    private int currentMoveIndex = -1;
    private String location;

    @Override
    public String toString() {
        return this.name;
    }

    public String formattedContent() {
        if (!StringUtils.isEmpty(parsingError)) {
            return parsingError;
        }
        StringBuilder gameText = new StringBuilder();
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
