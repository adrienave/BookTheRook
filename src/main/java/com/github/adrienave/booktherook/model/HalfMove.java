package com.github.adrienave.booktherook.model;

import javafx.util.Pair;
import lombok.Data;

@Data
public class HalfMove {
    private final String algebraicNotation;
    private final String coordinateNotation;

    public Pair<Square, Square> convertToBoardLocation() {
        Square startPosition = new Square(letterToIndex(coordinateNotation.charAt(0)), Character.getNumericValue(coordinateNotation.charAt(1)) - 1);
        Square endPosition = new Square(letterToIndex(coordinateNotation.charAt(2)), Character.getNumericValue(coordinateNotation.charAt(3)) - 1);

        return new Pair<>(startPosition, endPosition);
    }

    private int letterToIndex(char letter) {
        return letter - 'a';
    }
}
