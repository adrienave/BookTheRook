package com.github.adrienave.booktherook.model;

import com.github.adrienave.booktherook.util.Piece;
import com.github.adrienave.booktherook.util.Side;
import javafx.util.Pair;
import lombok.Data;

import java.util.Objects;

@Data
public class HalfMove {
    private final String algebraicNotation;
    private final String coordinateNotation;
    private final Side color;
    private Piece takenPiece;

    public Pair<Square, Square> convertToBoardLocation() {
        Square startPosition = new Square(letterToIndex(coordinateNotation.charAt(0)), Character.getNumericValue(coordinateNotation.charAt(1)) - 1);
        Square endPosition = new Square(letterToIndex(coordinateNotation.charAt(2)), Character.getNumericValue(coordinateNotation.charAt(3)) - 1);

        return new Pair<>(startPosition, endPosition);
    }

    public boolean isCastle() {
        return isKingSideCastle() || isQueenSideCastle();
    }

    public boolean isKingSideCastle() {
        return Objects.equals(algebraicNotation, "O-O");
    }

    public boolean isQueenSideCastle() {
        return Objects.equals(algebraicNotation, "O-O-O");
    }

    private int letterToIndex(char letter) {
        return letter - 'a';
    }
}
