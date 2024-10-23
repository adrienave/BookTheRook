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
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class GameService {
    private GameRecord activeGame;

    public static GameRecord parsePGN(String gameLocation) throws Exception {
        PgnHolder pgn = new PgnHolder(gameLocation);
        pgn.loadPgn();
        Game game = pgn.getGames().get(0);
        List<HalfMove> moves = chesslibMovesToHalfMoves(game.getHalfMoves());

        return GameRecord.builder().name(String.format("%s - %s (%s)", game.getWhitePlayer(), game.getBlackPlayer(), game.getResult().getDescription())).moves(moves).build();
    }

    public static String toSAN(String gameContentInput) {
        return gameContentInput.replace("\n", " ");
    }

    public void setActiveGameUpdatedMoves(String gameMovesSAN) {
        MoveList chesslibMoves = new MoveList();
        chesslibMoves.loadFromSan(gameMovesSAN);

        activeGame.setMoves(chesslibMovesToHalfMoves(chesslibMoves));
        activeGame.setCurrentMoveIndex(-1);
    }

    private static List<HalfMove> chesslibMovesToHalfMoves(MoveList game) {
        boolean isWhiteTurn = true;
        List<HalfMove> moves = new ArrayList<>();
        for (Move move : game) {
            Piece promotionPiece = null;
            PieceType maybePromotionPieceType = move.getPromotion().getPieceType();
            if (maybePromotionPieceType != null) {
                promotionPiece = convertChesslibPieceToPiece(maybePromotionPieceType);
            }
            moves.add(HalfMove.builder().algebraicNotation(move.getSan()).coordinateNotation(move.toString()).color(isWhiteTurn ? Side.WHITE : Side.BLACK).promotionPiece(promotionPiece).build());
            isWhiteTurn = !isWhiteTurn;
        }
        return moves;
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

    private static Piece convertChesslibPieceToPiece(PieceType chesslibType) {
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
