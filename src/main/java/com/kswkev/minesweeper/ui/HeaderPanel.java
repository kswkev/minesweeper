package com.kswkev.minesweeper.ui;

import com.kswkev.minesweeper.model.Board;
import com.kswkev.minesweeper.model.GameState;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

/** Mines-left counter, smiley reset button and elapsed-time display. */
final class HeaderPanel extends JPanel {

    private static final int MAX_DISPLAY = 999;
    private static final int MIN_DISPLAY = -99;

    private final JLabel minesLabel = createDisplay();
    private final JLabel timeLabel = createDisplay();
    private final JButton smileyButton = new JButton();
    private final Timer timer;

    private Board board;
    private int seconds;

    HeaderPanel(Runnable onReset) {
        super(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(4, 4, 4, 4),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLoweredBevelBorder(),
                        BorderFactory.createEmptyBorder(4, 6, 4, 6))));

        smileyButton.setFocusable(false);
        smileyButton.setMargin(new Insets(2, 2, 2, 2));
        smileyButton.setToolTipText("New game (F2)");
        smileyButton.addActionListener(e -> onReset.run());

        JPanel centre = new JPanel();
        centre.setOpaque(false);
        centre.add(smileyButton);

        add(minesLabel, BorderLayout.WEST);
        add(centre, BorderLayout.CENTER);
        add(timeLabel, BorderLayout.EAST);

        timer = new Timer(1000, e -> {
            if (seconds < MAX_DISPLAY) {
                seconds++;
                timeLabel.setText(format(seconds));
            }
        });
    }

    private static JLabel createDisplay() {
        JLabel label = new JLabel("000", SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(Color.BLACK);
        label.setForeground(Color.RED);
        label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        return label;
    }

    /** Binds the header to a fresh board and resets the counter, face and timer. */
    void reset(Board newBoard) {
        board = newBoard;
        board.addChangeListener(this::update);
        timer.stop();
        seconds = 0;
        timeLabel.setText(format(0));
        update();
    }

    void showWorried() {
        smileyButton.setIcon(new SmileyIcon(SmileyIcon.Face.WORRIED));
    }

    /** Seconds shown on the timer; frozen once the game ends. */
    int getElapsedSeconds() {
        return seconds;
    }

    /** Refreshes the counter, face and timer from the board's state. */
    void update() {
        minesLabel.setText(format(board.getRemainingMines()));

        GameState state = board.getState();
        if (board.isStarted() && seconds == 0) {
            // Classic Minesweeper counts the first second as soon as play starts.
            seconds = 1;
            timeLabel.setText(format(seconds));
            if (state == GameState.PLAYING) {
                timer.start();
            }
        }
        if (state != GameState.PLAYING) {
            timer.stop();
        }

        SmileyIcon.Face face = switch (state) {
            case WON -> SmileyIcon.Face.WON;
            case LOST -> SmileyIcon.Face.LOST;
            case PLAYING -> SmileyIcon.Face.NORMAL;
        };
        smileyButton.setIcon(new SmileyIcon(face));
    }

    private static String format(int value) {
        int clamped = Math.max(MIN_DISPLAY, Math.min(MAX_DISPLAY, value));
        return clamped < 0 ? String.format("-%02d", -clamped) : String.format("%03d", clamped);
    }
}
