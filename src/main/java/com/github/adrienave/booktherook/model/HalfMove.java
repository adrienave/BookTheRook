package com.github.adrienave.booktherook.model;

import com.github.adrienave.booktherook.util.Piece;
import com.github.adrienave.booktherook.util.Side;
import javafx.util.Pair;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HalfMove {
    private final String algebraicNotation;
    private final String coordinateNotation;
    private final Side color;
    private final Piece promotionPiece;
    private Piece takenPiece;
    @Builder.Default
    private boolean isEnPassant = false;

    public Pair<Square, Square> convertToBoardLocation(boolean isBoardWhiteOriented) {
        Square startPosition = new Square(letterToIndex(coordinateNotation.charAt(0), isBoardWhiteOriented), isBoardWhiteOriented ? Character.getNumericValue(coordinateNotation.charAt(1)) - 1 : 8 - Character.getNumericValue(coordinateNotation.charAt(1)));
        Square endPosition = new Square(letterToIndex(coordinateNotation.charAt(2), isBoardWhiteOriented), isBoardWhiteOriented ? Character.getNumericValue(coordinateNotation.charAt(3)) - 1 : 8 - Character.getNumericValue(coordinateNotation.charAt(3)));

        return new Pair<>(startPosition, endPosition);
    }

    public boolean isCastle() {
        return isQueenSideCastle() || isKingSideCastle();
    }

    public boolean isKingSideCastle() {
        return algebraicNotation.contains("O-O") && !isQueenSideCastle();
    }

    public boolean isQueenSideCastle() {
        return algebraicNotation.contains("O-O-O");
    }

    public boolean isTake() { return algebraicNotation.contains("x"); }

    private static int letterToIndex(char letter, boolean isProperOrder) {
        if (isProperOrder) {
            return letter - 'a';
        }
        return 'h' - letter;
    }
}
