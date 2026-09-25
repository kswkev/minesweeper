package com.kswkev.minesweeper.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BestTimesTest {

    private Preferences node;
    private BestTimes bestTimes;

    @BeforeEach
    void setUp() {
        node = PreferencesTestSupport.newNode();
        bestTimes = new BestTimes(node);
    }

    @AfterEach
    void tearDown() throws BackingStoreException {
        PreferencesTestSupport.remove(node);
    }

    @Test
    void anyTimeIsARecordWhenNoneStored() {
        assertTrue(bestTimes.get(Difficulty.BEGINNER).isEmpty());
        assertTrue(bestTimes.isRecord(Difficulty.BEGINNER, 999));
    }

    @Test
    void recordsAndComparesPerDifficulty() {
        bestTimes.record(Difficulty.BEGINNER, 42, "Kev");

        assertEquals(new BestTimes.Entry(42, "Kev"), bestTimes.get(Difficulty.BEGINNER).orElseThrow());
        assertTrue(bestTimes.isRecord(Difficulty.BEGINNER, 41));
        assertFalse(bestTimes.isRecord(Difficulty.BEGINNER, 42));
        assertFalse(bestTimes.isRecord(Difficulty.BEGINNER, 60));
        assertTrue(bestTimes.get(Difficulty.EXPERT).isEmpty());
    }

    @Test
    void persistsAcrossInstances() {
        bestTimes.record(Difficulty.EXPERT, 150, "Kev");

        assertEquals(150, new BestTimes(node).get(Difficulty.EXPERT).orElseThrow().seconds());
    }

    @Test
    void customNeverSetsRecords() {
        assertFalse(bestTimes.isRecord(Difficulty.CUSTOM, 1));
        assertTrue(bestTimes.get(Difficulty.CUSTOM).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> bestTimes.record(Difficulty.CUSTOM, 1, "x"));
    }

    @Test
    void resetClearsAllTimes() {
        bestTimes.record(Difficulty.BEGINNER, 10, "a");
        bestTimes.record(Difficulty.INTERMEDIATE, 20, "b");

        bestTimes.reset();

        assertTrue(bestTimes.get(Difficulty.BEGINNER).isEmpty());
        assertTrue(bestTimes.get(Difficulty.INTERMEDIATE).isEmpty());
    }
}
