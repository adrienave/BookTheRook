package com.github.adrienave.booktherook.model;

import javafx.util.Pair;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HalfMoveTest {

    @Test
    void second_square_does_not_have_same_file_when_move_is_to_another_file() {
        HalfMove move = HalfMove.builder().coordinateNotation("a1b1").build();

        assertThat(move.convertToBoardLocation(true)).extracting(Pair::getKey, Pair::getValue).contains(new Square(0, 0), new Square(1, 0));
    }

    @Test
    void second_square_does_not_have_same_rank_when_move_is_to_another_rank() {
        HalfMove move = HalfMove.builder().coordinateNotation("a1a2").build();

        assertThat(move.convertToBoardLocation(true)).extracting(Pair::getKey, Pair::getValue).contains(new Square(0, 0), new Square(0, 1));
    }

    @Test
    void squares_are_boundaries_when_move_is_one_corner_to_another_one() {
        HalfMove move = HalfMove.builder().coordinateNotation("h1a8").build();

        assertThat(move.convertToBoardLocation(true)).extracting(Pair::getKey, Pair::getValue).contains(new Square(7, 0), new Square(0, 7));
    }

    @Test
    void is_king_side_castle_only_when_algebraic_move_is_O_dash_O() {
        HalfMove move = HalfMove.builder().algebraicNotation("O-O").build();

        assertThat(move.isCastle()).isTrue();
        assertThat(move.isKingSideCastle()).isTrue();
        assertThat(move.isQueenSideCastle()).isFalse();
    }

    @Test
    void is_queen_side_castle_only_when_algebraic_move_is_O_dash_O_dash_O() {
        HalfMove move = HalfMove.builder().algebraicNotation("O-O-O").build();

        assertThat(move.isCastle()).isTrue();
        assertThat(move.isKingSideCastle()).isFalse();
        assertThat(move.isQueenSideCastle()).isTrue();
    }

    @Test
    void is_not_castle_when_algebraic_move_is_not_proper_castle() {
        HalfMove move = HalfMove.builder().algebraicNotation("c4").build();

        assertThat(move.isCastle()).isFalse();
        assertThat(move.isKingSideCastle()).isFalse();
        assertThat(move.isQueenSideCastle()).isFalse();
    }

    @Test
    void is_capture_when_algebraic_move_contains_x() {
        HalfMove move = HalfMove.builder().algebraicNotation("cxd4").build();

        assertThat(move.isTake()).isTrue();
    }

    @Test
    void is_not_capture_when_algebraic_move_does_not_contain_x() {
        HalfMove move = HalfMove.builder().algebraicNotation("c4").build();

        assertThat(move.isTake()).isFalse();
    }
}