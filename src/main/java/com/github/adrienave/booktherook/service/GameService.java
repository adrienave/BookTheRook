package com.github.adrienave.booktherook.service;

import com.github.adrienave.booktherook.model.GameRecord;
import com.github.adrienave.booktherook.model.HalfMove;
import com.github.adrienave.booktherook.util.Piece;
import com.github.adrienave.booktherook.util.Side;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@Getter
@Setter
public class GameService {
    private GameRecord activeGame;

    public GameRecord parsePGN(String gameLocation) throws Exception {
        PgnHolder pgn = new PgnHolder(gameLocation);
        pgn.loadPgn();
        Game game = pgn.getGames().get(0);

        boolean isWhiteTurn = true;
        List<HalfMove> moves = new ArrayList<>();
        for (Move move : game.getHalfMoves()) {
            Piece promotionPiece = null;
            PieceType maybePromotionPieceType = move.getPromotion().getPieceType();
            if (maybePromotionPieceType != null) {
                promotionPiece = convertChesslibPieceToPiece(maybePromotionPieceType);
            }
            moves.add(new HalfMove(move.getSan(), move.toString(), isWhiteTurn ? Side.WHITE : Side.BLACK, promotionPiece));
            isWhiteTurn = !isWhiteTurn;
        }

        return new GameRecord(String.format("%s - %s (%s)", game.getWhitePlayer(), game.getBlackPlayer(), game.getResult().getDescription()), moves);
    }

    public static String toSAN(String gameContentInput) {
        return gameContentInput.replace("\n", " ");
    }

    public void setActiveGameUpdatedMoves(String gameMovesSAN) {
        MoveList chesslibMoves = new MoveList();
        chesslibMoves.loadFromSan(gameMovesSAN);

        boolean isWhiteTurn = true;
        List<HalfMove> updatedMoves = new ArrayList<>();
        for (Move move : chesslibMoves) {
            Piece promotionPiece = null;
            PieceType maybePromotionPieceType = move.getPromotion().getPieceType();
            if (maybePromotionPieceType != null) {
                promotionPiece = convertChesslibPieceToPiece(maybePromotionPieceType);
            }
            updatedMoves.add(new HalfMove(move.getSan(), move.toString(), isWhiteTurn ? Side.WHITE : Side.BLACK, promotionPiece));
            isWhiteTurn = !isWhiteTurn;
        }

        activeGame.setMoves(updatedMoves);
        activeGame.setCurrentMoveIndex(-1);
    }

    public Optional<HalfMove> updateActiveMove(boolean switchToNext) {
        if (switchToNext && activeGame.getCurrentMoveIndex() < activeGame.getMoves().size() - 1) {
            activeGame.setCurrentMoveIndex(activeGame.getCurrentMoveIndex() + 1);
            return Optional.of(activeGame.getMoves().get(activeGame.getCurrentMoveIndex()));
        } else if (!switchToNext && activeGame.getCurrentMoveIndex() > -1) {
            HalfMove activeMove = activeGame.getMoves().get(activeGame.getCurrentMoveIndex());
            activeGame.setCurrentMoveIndex(activeGame.getCurrentMoveIndex() - 1);
            return Optional.of(activeMove);
        }
        return Optional.empty();
    }

    private Piece convertChesslibPieceToPiece(PieceType chesslibType) {
        return switch (chesslibType) {
            case PAWN -> Piece.PAWN;
            case ROOK -> Piece.ROOK;
            case KNIGHT -> Piece.KNIGHT;
            case BISHOP -> Piece.BISHOP;
            case QUEEN -> Piece.QUEEN;
            case KING -> Piece.KING;
            case NONE -> null;
        };
    }
}
