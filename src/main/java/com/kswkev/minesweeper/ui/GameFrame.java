package com.kswkev.minesweeper.ui;

import com.kswkev.minesweeper.model.Board;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;

public final class GameFrame extends JFrame {

    private static final int ROWS = 9;
    private static final int COLS = 9;
    private static final int MINES = 10;

    private BoardPanel boardPanel;

    public GameFrame() {
        super("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setJMenuBar(createMenuBar());
        newGame();
        setLocationRelativeTo(null);
    }

    private JMenuBar createMenuBar() {
        JMenuItem newGame = new JMenuItem("New Game");
        newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        newGame.addActionListener(e -> newGame());

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> dispose());

        JMenu game = new JMenu("Game");
        game.setMnemonic(KeyEvent.VK_G);
        game.add(newGame);
        game.addSeparator();
        game.add(exit);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(game);
        return menuBar;
    }

    private void newGame() {
        if (boardPanel != null) {
            remove(boardPanel);
        }
        boardPanel = new BoardPanel(new Board(ROWS, COLS, MINES));
        add(boardPanel);
        pack();
    }
}