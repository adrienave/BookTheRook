package com.github.adrienave.booktherook.service;

import com.github.adrienave.booktherook.model.GameRecord;
import com.github.adrienave.booktherook.model.HalfMove;
import com.github.adrienave.booktherook.util.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.game.*;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GameServiceTest {
    private final GameService gameService = spy(new GameService());

    @Test
    void returns_parsed_game_when_game_location_exists() throws Exception {
        String gameLocation = "/realChessGame";
        MoveList halfMoves = new MoveList();
        Move firstMove = new Move(Square.C2, Square.C4);
        firstMove.setSan("c4");
        halfMoves.add(firstMove);
        Move firstBlackMove = new Move(Square.E7, Square.E5);
        firstBlackMove.setSan("e5");
        halfMoves.add(firstBlackMove);
        List<HalfMove> moves = List.of(HalfMove.builder().algebraicNotation(firstMove.getSan()).coordinateNotation(firstMove.toString()).color(Side.WHITE).build(), HalfMove.builder().algebraicNotation(firstBlackMove.getSan()).coordinateNotation(firstBlackMove.toString()).color(Side.BLACK).build());
        String eventName = "Internal Tournament";
        String whitePlayerName = "Oswaldo";
        String blackPlayerName = "Mistouille";
        GameResult gameResult = GameResult.WHITE_WON;
        PgnHolder pgnHolder = givenPgnHolderWithValidGame(eventName, whitePlayerName, blackPlayerName, gameResult, halfMoves);
        when(gameService.getPgnHolder(anyString())).thenReturn(pgnHolder);

        GameRecord gameRecord = gameService.parsePGN(gameLocation);

        verify(pgnHolder).loadPgn();
        assertThat(gameRecord.getMoves()).containsAll(moves);
        assertThat(gameRecord.getEventName()).isEqualTo(eventName);
        assertThat(gameRecord.getWhitePlayerName()).isEqualTo(whitePlayerName);
        assertThat(gameRecord.getBlackPlayerName()).isEqualTo(blackPlayerName);
        assertThat(gameRecord.getResult()).isEqualTo(gameResult.getDescription());
    }

    private static PgnHolder givenPgnHolderWithValidGame(String eventName, String whitePlayerName, String blackPlayerName, GameResult gameResult, MoveList moves) {
        PgnHolder pgnHolder = mock(PgnHolder.class);
        Event event = new Event();
        event.setName(eventName);
        Game game = new Game("1", new Round(event));
        game.setWhitePlayer(new GenericPlayer("42", whitePlayerName));
        game.setBlackPlayer(new GenericPlayer("45", blackPlayerName));
        game.setResult(gameResult);
        game.setHalfMoves(moves);
        when(pgnHolder.getGames()).thenReturn(List.of(game));
        return pgnHolder;
    }
}