package com.kswkev.minesweeper.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsTest {

    private Preferences node;
    private Settings settings;

    @BeforeEach
    void setUp() {
        node = PreferencesTestSupport.newNode();
        settings = new Settings(node);
    }

    @AfterEach
    void tearDown() throws BackingStoreException {
        PreferencesTestSupport.remove(node);
    }

    @Test
    void defaultsWhenNothingStored() {
        assertEquals(Difficulty.BEGINNER, settings.getDifficulty());
        assertEquals(Difficulty.BEGINNER.getPreset().orElseThrow(), settings.getBoardConfig());
        assertTrue(settings.isQuestionMarksEnabled());
        assertEquals("Anonymous", settings.getLastName());
    }

    @Test
    void remembersValuesAcrossInstances() {
        settings.setDifficulty(Difficulty.EXPERT);
        settings.setQuestionMarksEnabled(false);
        settings.setLastName("Kev");

        Settings reloaded = new Settings(node);
        assertEquals(Difficulty.EXPERT, reloaded.getDifficulty());
        assertEquals(Difficulty.EXPERT.getPreset().orElseThrow(), reloaded.getBoardConfig());
        assertFalse(reloaded.isQuestionMarksEnabled());
        assertEquals("Kev", reloaded.getLastName());
    }

    @Test
    void customDifficultyUsesStoredCustomBoard() {
        BoardConfig custom = new BoardConfig(12, 20, 50);
        settings.setCustomConfig(custom);
        settings.setDifficulty(Difficulty.CUSTOM);

        assertEquals(custom, new Settings(node).getBoardConfig());
    }

    @Test
    void invalidStoredValuesFallBackToDefaults() {
        node.put("difficulty", "NIGHTMARE");
        node.putInt("custom.rows", 500);

        assertEquals(Difficulty.BEGINNER, settings.getDifficulty());
        assertEquals(Difficulty.BEGINNER.getPreset().orElseThrow(), settings.getCustomConfig());
    }
}
