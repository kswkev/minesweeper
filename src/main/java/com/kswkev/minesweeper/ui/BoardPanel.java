package com.kswkev.minesweeper.ui;

import com.kswkev.minesweeper.model.Board;
import com.kswkev.minesweeper.model.GameState;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Grid of {@link CellButton}s bound to a {@link Board}. */
final class BoardPanel extends JPanel {

    /** Notified of mouse activity so the header can update its face, counter and timer. */
    interface Listener {
        /** A reveal or chord is being held down on a cell. */
        void cellPressed();

        /** The mouse was released; the board may have changed. */
        void boardChanged();
    }

    private final Board board;
    private final Listener listener;
    private final CellButton[][] buttons;

    BoardPanel(Board board, Listener listener) {
        super(new GridLayout(board.getRows(), board.getCols()));
        this.board = board;
        this.listener = listener;
        this.buttons = new CellButton[board.getRows()][board.getCols()];

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (board.getState() == GameState.PLAYING && !SwingUtilities.isRightMouseButton(e)) {
                    listener.cellPressed();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                CellButton button = (CellButton) e.getSource();
                // Released outside the cell: treat as cancelled.
                if (button.contains(e.getPoint())) {
                    handleClick(button, e);
                }
                listener.boardChanged();
            }
        };

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                CellButton button = new CellButton(r, c);
                button.addMouseListener(mouseHandler);
                buttons[r][c] = button;
                add(button);
            }
        }
        refresh();
    }

    private void handleClick(CellButton button, MouseEvent e) {
        if (board.getState() != GameState.PLAYING) {
            return;
        }
        int row = button.row;
        int col = button.col;
        if (SwingUtilities.isMiddleMouseButton(e)
                || (SwingUtilities.isLeftMouseButton(e) && board.getCell(row, col).isRevealed())) {
            board.chord(row, col);
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            board.reveal(row, col);
        } else if (SwingUtilities.isRightMouseButton(e)) {
            board.toggleFlag(row, col);
        } else {
            return;
        }
        refresh();
    }

    private void refresh() {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                buttons[r][c].render(board.getCell(r, c), board.getState(), board.isExploded(r, c));
            }
        }
    }
}
