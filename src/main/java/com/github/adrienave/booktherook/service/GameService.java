package com.github.adrienave.booktherook.service;

import com.github.adrienave.booktherook.model.GameRecord;
import com.github.adrienave.booktherook.model.HalfMove;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class GameService {

    public GameRecord parsePGN(String gameLocation) throws Exception {
        PgnHolder pgn = new PgnHolder(gameLocation);
        pgn.loadPgn();
        Game game = pgn.getGames().get(0);
        List<HalfMove> moves = game.getHalfMoves().stream()
                .map(move -> new HalfMove(move.getSan(), move.toString()))
                .collect(Collectors.toList());

        return new GameRecord(String.format("%s - %s (%s)", game.getWhitePlayer(), game.getBlackPlayer(), game.getResult().getDescription()), moves);
    }
}
