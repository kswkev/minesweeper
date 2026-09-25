package com.kswkev.minesweeper;

import com.kswkev.minesweeper.model.BestTimes;
import com.kswkev.minesweeper.model.GameSession;
import com.kswkev.minesweeper.model.Settings;
import com.kswkev.minesweeper.ui.GameFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.time.Clock;
import java.util.prefs.Preferences;

public final class Main {

    private static final String PREFS_NODE = "com/kswkev/minesweeper";

    private Main() {
    }

    public static void main(String[] args) {
        Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
        Settings settings = new Settings(prefs);
        BestTimes bestTimes = new BestTimes(prefs);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to the default look and feel.
            }
            new GameFrame(new GameSession(settings, bestTimes, Clock.systemUTC())).setVisible(true);
        });
    }
}
