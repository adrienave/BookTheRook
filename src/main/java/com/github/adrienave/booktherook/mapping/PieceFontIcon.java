package com.github.adrienave.booktherook.mapping;

import com.github.adrienave.booktherook.util.Piece;
import com.github.adrienave.booktherook.util.Side;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import static org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.*;

public class PieceFontIcon {
    public static FontIcon createVisualPiece(Piece pieceName, Side side) {
        FontIcon pieceIcon = new FontIcon(QUESTION);
        switch (pieceName) {
            case PAWN -> pieceIcon = new FontIcon(CHESS_PAWN);
            case ROOK -> pieceIcon = new FontIcon(CHESS_ROOK);
            case KNIGHT -> pieceIcon = new FontIcon(CHESS_KNIGHT);
            case BISHOP -> pieceIcon = new FontIcon(CHESS_BISHOP);
            case QUEEN -> pieceIcon = new FontIcon(CHESS_QUEEN);
            case KING -> pieceIcon = new FontIcon(CHESS_KING);
        }
        switch (side) {
            case BLACK -> pieceIcon.setIconColor(Color.BLACK);
            case WHITE -> pieceIcon.setIconColor(Color.WHITE);
        }
        pieceIcon.setIconSize(30);
        pieceIcon.setStroke(Color.BLACK);
        return pieceIcon;
    }

    public static Piece convertNodeToPiece(Node node) {
        if (!(node instanceof FontIcon pieceRepresentation)) {
            throw new RuntimeException(String.format("Node %s is not a valid Piece representation", node));
        }
        if (!(pieceRepresentation.getIconCode() instanceof FontAwesomeSolid pieceIcon)) {
            throw new RuntimeException(String.format("Node %s is not a valid Piece representation", node));
        }
        return switch (pieceIcon) {
            case CHESS_PAWN -> Piece.PAWN;
            case CHESS_ROOK -> Piece.ROOK;
            case CHESS_KNIGHT -> Piece.KNIGHT;
            case CHESS_BISHOP -> Piece.BISHOP;
            case CHESS_QUEEN -> Piece.QUEEN;
            case CHESS_KING -> Piece.KING;
            default ->
                    throw new RuntimeException(String.format("Icon %s is not a valid Piece representation", pieceRepresentation.getIconLiteral()));
        };
    }
}
