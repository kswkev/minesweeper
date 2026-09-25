package com.kswkev.minesweeper.ui;

import com.kswkev.minesweeper.model.Board;
import com.kswkev.minesweeper.model.BoardConfig;
import com.kswkev.minesweeper.model.Difficulty;
import com.kswkev.minesweeper.model.GameSession;

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

/** The game window. Game rules live in {@link GameSession}; this class only shows them. */
public final class GameFrame extends JFrame implements GameSession.Listener {

    private final GameSession session;
    private final HeaderPanel headerPanel;
    private final Map<Difficulty, JRadioButtonMenuItem> difficultyItems = new EnumMap<>(Difficulty.class);

    private BoardPanel boardPanel;

    public GameFrame(GameSession session) {
        this.session = session;
        this.headerPanel = new HeaderPanel(session);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setJMenuBar(createMenuBar());
        add(headerPanel, BorderLayout.NORTH);

        session.setListener(this);
        gameStarted(session.getBoard());
        setLocationRelativeTo(null);
    }

    private JMenuBar createMenuBar() {
        JMenuItem newGame = new JMenuItem("New Game");
        newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        newGame.addActionListener(e -> session.newGame());

        ButtonGroup group = new ButtonGroup();
        for (Difficulty level : Difficulty.values()) {
            String label = level == Difficulty.CUSTOM ? "Custom..." : level.getDisplayName();
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(label);
            item.addActionListener(e -> selectDifficulty(level));
            group.add(item);
            difficultyItems.put(level, item);
        }
        difficultyItems.get(session.getDifficulty()).setSelected(true);

        JCheckBoxMenuItem marks = new JCheckBoxMenuItem("Marks (?)",
                session.getSettings().isQuestionMarksEnabled());
        marks.addActionListener(e -> session.setQuestionMarksEnabled(marks.isSelected()));

        JMenuItem bestTimesItem = new JMenuItem("Best Times...");
        bestTimesItem.addActionListener(e -> BestTimesDialog.show(this, session.getBestTimes()));

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
            var chosen = CustomBoardDialog.show(this, session.getSettings().getCustomConfig());
            if (chosen.isEmpty()) {
                // Cancelled: keep the current game and its menu selection.
                difficultyItems.get(session.getDifficulty()).setSelected(true);
                return;
            }
            config = chosen.get();
        } else {
            config = level.getPreset().orElseThrow();
        }
        session.selectDifficulty(level, config);
        setLocationRelativeTo(null);
    }

    @Override
    public void gameStarted(Board board) {
        if (boardPanel != null) {
            remove(boardPanel);
        }
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
        add(boardPanel, BorderLayout.CENTER);
        setTitle(session.getTitle());
        pack();
    }

    @Override
    public void recordAchieved(Difficulty level, int seconds) {
        // Let the finished board paint before the prompt appears.
        SwingUtilities.invokeLater(() -> promptForRecord(level, seconds));
    }

    private void promptForRecord(Difficulty level, int seconds) {
        String message = "You have the fastest time for " + level.getDisplayName().toLowerCase()
                + " level (" + seconds + " seconds).\nPlease enter your name:";
        Object input = JOptionPane.showInputDialog(this, message, "New Best Time",
                JOptionPane.INFORMATION_MESSAGE, null, null, session.getSettings().getLastName());
        if (input == null) {
            return;
        }
        session.saveBestTime(level, seconds, input.toString());
        BestTimesDialog.show(this, session.getBestTimes());
    }
}
