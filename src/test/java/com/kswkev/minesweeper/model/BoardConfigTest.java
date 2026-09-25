package com.kswkev.minesweeper.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BoardConfigTest {

    @Test
    void presetsMatchClassicSizes() {
        assertEquals(new BoardConfig(9, 9, 10), BoardConfig.BEGINNER);
        assertEquals(new BoardConfig(16, 16, 40), BoardConfig.INTERMEDIATE);
        assertEquals(new BoardConfig(16, 30, 99), BoardConfig.EXPERT);
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
}
