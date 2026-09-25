package com.kswkev.minesweeper.ui;

import com.kswkev.minesweeper.model.BestTimes;
import com.kswkev.minesweeper.model.Board;
import com.kswkev.minesweeper.model.BoardConfig;
import com.kswkev.minesweeper.model.Difficulty;
import com.kswkev.minesweeper.model.GameState;
import com.kswkev.minesweeper.model.MineProgression;
import com.kswkev.minesweeper.model.Settings;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.EnumMap;
import java.util.Map;

public final class GameFrame extends JFrame {

    private final Settings settings;
    private final BestTimes bestTimes;
    private final HeaderPanel headerPanel = new HeaderPanel(this::newGame);
    private final Map<Difficulty, JRadioButtonMenuItem> difficultyItems = new EnumMap<>(Difficulty.class);

    private Difficulty difficulty;
    private final MineProgression progression;
    private Board board;
    private BoardPanel boardPanel;
    private boolean resultHandled;

    public GameFrame(Settings settings, BestTimes bestTimes) {
        this.settings = settings;
        this.bestTimes = bestTimes;
        this.difficulty = settings.getDifficulty();
        this.progression = new MineProgression(settings.getBoardConfig());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setJMenuBar(createMenuBar());
        add(headerPanel, BorderLayout.NORTH);
        startGame();
        setLocationRelativeTo(null);
    }

    private JMenuBar createMenuBar() {
        JMenuItem newGame = new JMenuItem("New Game");
        newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        newGame.addActionListener(e -> newGame());

        ButtonGroup group = new ButtonGroup();
        for (Difficulty level : Difficulty.values()) {
            String label = level == Difficulty.CUSTOM ? "Custom..." : level.getDisplayName();
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(label);
            item.addActionListener(e -> selectDifficulty(level));
            group.add(item);
            difficultyItems.put(level, item);
        }
        difficultyItems.get(difficulty).setSelected(true);

        JCheckBoxMenuItem marks = new JCheckBoxMenuItem("Marks (?)", settings.isQuestionMarksEnabled());
        marks.addActionListener(e -> {
            settings.setQuestionMarksEnabled(marks.isSelected());
            board.setQuestionMarksEnabled(marks.isSelected());
        });

        JMenuItem bestTimesItem = new JMenuItem("Best Times...");
        bestTimesItem.addActionListener(e -> BestTimesDialog.show(this, bestTimes));

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        JMenu game = new JMenu("Game");
        game.setMnemonic(KeyEvent.VK_G);
        game.add(newGame);
        game.addSeparator();
        for (Difficulty level : Difficulty.values()) {
            game.add(difficultyItems.get(level));
        }
        game.addSeparator();
        game.add(marks);
        game.addSeparator();
        game.add(bestTimesItem);
        game.addSeparator();
        game.add(exit);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(game);
        return menuBar;
    }

    private void selectDifficulty(Difficulty level) {
        BoardConfig config;
        if (level == Difficulty.CUSTOM) {
            var chosen = CustomBoardDialog.show(this, settings.getCustomConfig());
            if (chosen.isEmpty()) {
                // Cancelled: keep the current game and its menu selection.
                difficultyItems.get(difficulty).setSelected(true);
                return;
            }
            config = chosen.get();
            settings.setCustomConfig(config);
        } else {
            config = level.getPreset().orElseThrow();
        }
        difficulty = level;
        settings.setDifficulty(level);
        progression.reset(config);
        startGame();
        setLocationRelativeTo(null);
    }

    /** New Game / smiley: leaving a game in progress counts as abandoning it. */
    private void newGame() {
        if (board != null && board.isStarted() && board.getState() == GameState.PLAYING) {
            progression.onAbandon();
        }
        startGame();
    }

    private void startGame() {
        if (boardPanel != null) {
            remove(boardPanel);
        }
        board = new Board(progression.current());
        board.setQuestionMarksEnabled(settings.isQuestionMarksEnabled());
        resultHandled = false;

        // Listener order matters: redraw the grid and stop the timer before handling the result.
        boardPanel = new BoardPanel(board, new BoardPanel.PressListener() {
            @Override
            public void cellPressed() {
                headerPanel.showWorried();
            }

            @Override
            public void cellReleased() {
                headerPanel.update();
            }
        });
        headerPanel.reset(board);
        board.addChangeListener(this::onBoardChanged);

        add(boardPanel, BorderLayout.CENTER);
        updateTitle();
        pack();
    }

    private void onBoardChanged() {
        GameState state = board.getState();
        if (state == GameState.PLAYING || resultHandled) {
            return;
        }
        resultHandled = true;
        if (state == GameState.LOST) {
            progression.onLoss();
            return;
        }

        // Only standard games (preset difficulty, no extra mines) can set a best time.
        boolean recordEligible = progression.getExtraMines() == 0;
        progression.onWin();
        int seconds = headerPanel.getElapsedSeconds();
        if (recordEligible && bestTimes.isRecord(difficulty, seconds)) {
            Difficulty level = difficulty;
            // Let the finished board paint before the prompt appears.
            SwingUtilities.invokeLater(() -> promptForRecord(level, seconds));
        }
    }

    private void promptForRecord(Difficulty level, int seconds) {
        String message = "You have the fastest time for " + level.getDisplayName().toLowerCase()
                + " level (" + seconds + " seconds).\nPlease enter your name:";
        Object input = JOptionPane.showInputDialog(this, message, "New Best Time",
                JOptionPane.INFORMATION_MESSAGE, null, null, settings.getLastName());
        if (input == null) {
            return;
        }
        String name = input.toString().isBlank() ? "Anonymous" : input.toString().trim();
        settings.setLastName(name);
        bestTimes.record(level, seconds, name);
        BestTimesDialog.show(this, bestTimes);
    }

    private void updateTitle() {
        int extra = progression.getExtraMines();
        setTitle("Minesweeper: " + difficulty.getDisplayName() + (extra > 0 ? " +" + extra : ""));
    }
}
