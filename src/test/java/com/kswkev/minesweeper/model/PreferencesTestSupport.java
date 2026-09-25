package com.kswkev.minesweeper.model;

import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/** Throwaway preference nodes so tests never touch the player's real settings. */
final class PreferencesTestSupport {

    private PreferencesTestSupport() {
    }

    static Preferences newNode() {
        return Preferences.userRoot().node("com/kswkev/minesweeper-test/" + UUID.randomUUID());
    }

    static void remove(Preferences node) throws BackingStoreException {
        Preferences parent = node.parent();
        node.removeNode();
        if (parent.childrenNames().length == 0 && parent.keys().length == 0) {
            parent.removeNode();
        }
        parent.flush();
    }
}
