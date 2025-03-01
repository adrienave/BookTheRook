package com.github.adrienave.booktherook.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class GameRecordTest {

    @Test
    void returns_name_when_converting_to_string() {
        String gameName = "My game title";
        var gameRecord = GameRecord.builder().name(gameName).build();

        assertThat(gameRecord.toString()).isEqualTo(gameName);
    }

    @Test
    void formattedContent_returns_moves_in_sequence_when_no_parsingError() {
        List<HalfMove> moves = List.of(HalfMove.builder().algebraicNotation("c4").build(), HalfMove.builder().algebraicNotation("e5").build());
        var gameRecord = GameRecord.builder().moves(moves).build();

        assertThat(gameRecord.formattedContent()).isEqualTo("1. c4 e5");
    }

    @Test
    void formattedContent_returns_separated_moves_in_different_lines_when_half_moves_are_more_than_2() {
        List<HalfMove> moves = List.of(HalfMove.builder().algebraicNotation("c4").build(), HalfMove.builder().algebraicNotation("e5").build(), HalfMove.builder().algebraicNotation("g3").build());
        var gameRecord = GameRecord.builder().moves(moves).build();

        assertThat(gameRecord.formattedContent()).isEqualTo("1. c4 e5\n2. g3");
    }

    @Test
    void formattedContent_returns_parsingError_when_parsingError_is_defined() {
        String parsingError = "PGN content is invalid";
        List<HalfMove> moves = List.of(HalfMove.builder().algebraicNotation("c4").build(), HalfMove.builder().algebraicNotation("e5").build());
        var gameRecord = GameRecord.builder().parsingError(parsingError).moves(moves).build();

        assertThat(gameRecord.formattedContent()).isEqualTo(parsingError);
    }
}