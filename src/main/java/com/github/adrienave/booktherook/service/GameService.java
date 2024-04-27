package com.github.adrienave.booktherook.service;

import com.github.adrienave.booktherook.model.GameRecord;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
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
        List<String> moves = game.getHalfMoves().stream().map(Move::getSan).collect(Collectors.toList());

        return new GameRecord(String.format("%s - %s", game.getWhitePlayer(), game.getBlackPlayer()), moves);
    }
}
