package com.github.adrienave.booktherook.service;

import com.github.adrienave.booktherook.model.GameRecord;
import com.github.adrienave.booktherook.model.HalfMove;
import com.github.adrienave.booktherook.util.Side;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
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
            moves.add(new HalfMove(move.getSan(), move.toString(), isWhiteTurn ? Side.WHITE : Side.BLACK));
            isWhiteTurn = !isWhiteTurn;
        }

        return new GameRecord(String.format("%s - %s (%s)", game.getWhitePlayer(), game.getBlackPlayer(), game.getResult().getDescription()), moves);
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
}
