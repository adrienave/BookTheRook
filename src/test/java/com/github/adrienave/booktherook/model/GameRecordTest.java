package com.github.adrienave.booktherook.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class GameRecordTest {

    @Test
    void returns_name_when_converting_to_string() {
        String gameName = "My game title";
        var gameRecord = GameRecord.builder().name(gameName).build();

        assertThat(gameRecord.toString()).isEqualTo(gameName);
    }
}