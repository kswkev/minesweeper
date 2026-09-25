package com.kswkev.minesweeper.ui;

import com.kswkev.minesweeper.model.Board;
import com.kswkev.minesweeper.model.BoardConfig;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

public final class GameFrame extends JFrame {

    private final HeaderPanel headerPanel = new HeaderPanel(this::newGame);
    private final ButtonGroup difficultyGroup = new ButtonGroup();

    private BoardConfig config = BoardConfig.BEGINNER;
    private JRadioButtonMenuItem selectedDifficulty;
    private BoardPanel boardPanel;

    public GameFrame() {
        super("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setJMenuBar(createMenuBar());
        add(headerPanel, BorderLayout.NORTH);
        newGame();
        setLocationRelativeTo(null);
    }

    private JMenuBar createMenuBar() {
        JMenuItem newGame = new JMenuItem("New Game");
        newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        newGame.addActionListener(e -> newGame());

        JRadioButtonMenuItem beginner = difficultyItem("Beginner", BoardConfig.BEGINNER);
        JRadioButtonMenuItem intermediate = difficultyItem("Intermediate", BoardConfig.INTERMEDIATE);
        JRadioButtonMenuItem expert = difficultyItem("Expert", BoardConfig.EXPERT);
        JRadioButtonMenuItem custom = new JRadioButtonMenuItem("Custom...");
        custom.addActionListener(e -> chooseCustom(custom));
        difficultyGroup.add(custom);

        beginner.setSelected(true);
        selectedDifficulty = beginner;

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        JMenu game = new JMenu("Game");
        game.setMnemonic(KeyEvent.VK_G);
        game.add(newGame);
        game.addSeparator();
        game.add(beginner);
        game.add(intermediate);
        game.add(expert);
        game.add(custom);
        game.addSeparator();
        game.add(exit);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(game);
        return menuBar;
    }

    private JRadioButtonMenuItem difficultyItem(String label, BoardConfig preset) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(label);
        item.addActionListener(e -> changeDifficulty(item, preset));
        difficultyGroup.add(item);
        return item;
    }

    private void chooseCustom(JRadioButtonMenuItem customItem) {
        CustomBoardDialog.show(this, config).ifPresentOrElse(
                chosen -> changeDifficulty(customItem, chosen),
                // Cancelled: keep the current game and its menu selection.
                () -> selectedDifficulty.setSelected(true));
    }

    private void changeDifficulty(JRadioButtonMenuItem item, BoardConfig newConfig) {
        selectedDifficulty = item;
        config = newConfig;
        newGame();
        setLocationRelativeTo(null);
    }

    private void newGame() {
        if (boardPanel != null) {
            remove(boardPanel);
        }
        Board board = new Board(config);
        boardPanel = new BoardPanel(board, new BoardPanel.Listener() {
            @Override
            public void cellPressed() {
                headerPanel.showWorried();
            }

            @Override
            public void boardChanged() {
                headerPanel.update();
            }
        });
        headerPanel.reset(board);
        add(boardPanel, BorderLayout.CENTER);
        pack();
    }
}
