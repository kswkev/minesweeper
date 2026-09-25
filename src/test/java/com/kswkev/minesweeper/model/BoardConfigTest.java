package com.kswkev.minesweeper.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardConfigTest {

    @Test
    void presetsMatchClassicSizes() {
        assertEquals(new BoardConfig(9, 9, 10), Difficulty.BEGINNER.getPreset().orElseThrow());
        assertEquals(new BoardConfig(16, 16, 40), Difficulty.INTERMEDIATE.getPreset().orElseThrow());
        assertEquals(new BoardConfig(16, 30, 99), Difficulty.EXPERT.getPreset().orElseThrow());
        assertTrue(Difficulty.CUSTOM.getPreset().isEmpty());
    }

    @Test
    void acceptsBoundaryValues() {
        assertDoesNotThrow(() -> new BoardConfig(5, 5, 1));
        assertDoesNotThrow(() -> new BoardConfig(5, 5, 16));
        assertDoesNotThrow(() -> new BoardConfig(24, 30, 24 * 30 - 9));
    }

    @Test
    void rejectsOutOfRangeValues() {
        assertThrows(IllegalArgumentException.class, () -> new BoardConfig(4, 9, 10));
        assertThrows(IllegalArgumentException.class, () -> new BoardConfig(25, 9, 10));
        assertThrows(IllegalArgumentException.class, () -> new BoardConfig(9, 4, 10));
        assertThrows(IllegalArgumentException.class, () -> new BoardConfig(9, 31, 10));
        assertThrows(IllegalArgumentException.class, () -> new BoardConfig(9, 9, 0));
        assertThrows(IllegalArgumentException.class, () -> new BoardConfig(5, 5, 17));
    }

    @Test
    void withMinesKeepsSizeAndValidates() {
        BoardConfig config = new BoardConfig(9, 9, 10).withMines(11);

        assertEquals(new BoardConfig(9, 9, 11), config);
        assertEquals(72, config.maxMines());
        assertThrows(IllegalArgumentException.class, () -> config.withMines(73));
    }
}
