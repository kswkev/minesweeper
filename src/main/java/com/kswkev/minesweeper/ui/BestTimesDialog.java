package com.kswkev.minesweeper.ui;

import com.kswkev.minesweeper.model.BestTimes;
import com.kswkev.minesweeper.model.Difficulty;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/** Shows the fastest time for each preset difficulty, with an option to clear them. */
final class BestTimesDialog extends JDialog {

    private static final Difficulty[] LEVELS = {Difficulty.BEGINNER, Difficulty.INTERMEDIATE, Difficulty.EXPERT};

    private final BestTimes bestTimes;
    private final JLabel[] timeLabels = new JLabel[LEVELS.length];
    private final JLabel[] nameLabels = new JLabel[LEVELS.length];

    private BestTimesDialog(JFrame owner, BestTimes bestTimes) {
        super(owner, "Best Times", true);
        this.bestTimes = bestTimes;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel rows = new JPanel(new GridLayout(LEVELS.length, 3, 16, 6));
        rows.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        for (int i = 0; i < LEVELS.length; i++) {
            timeLabels[i] = new JLabel();
            nameLabels[i] = new JLabel();
            rows.add(new JLabel(LEVELS[i].getDisplayName() + ":"));
            rows.add(timeLabels[i]);
            rows.add(nameLabels[i]);
        }
        refresh();

        JButton reset = new JButton("Reset Scores");
        reset.addActionListener(e -> {
            int answer = JOptionPane.showConfirmDialog(this, "Clear all best times?", "Best Times",
                    JOptionPane.OK_CANCEL_OPTION);
            if (answer == JOptionPane.OK_OPTION) {
                bestTimes.reset();
                refresh();
            }
        });
        JButton ok = new JButton("OK");
        ok.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(reset);
        buttons.add(ok);

        add(rows, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(ok);
        pack();
        setLocationRelativeTo(owner);
    }

    static void show(JFrame owner, BestTimes bestTimes) {
        new BestTimesDialog(owner, bestTimes).setVisible(true);
    }

    private void refresh() {
        for (int i = 0; i < LEVELS.length; i++) {
            var entry = bestTimes.get(LEVELS[i]);
            timeLabels[i].setText(entry.map(best -> best.seconds() + " seconds").orElse("999 seconds"));
            nameLabels[i].setText(entry.map(BestTimes.Entry::name).orElse("Anonymous"));
        }
    }
}
