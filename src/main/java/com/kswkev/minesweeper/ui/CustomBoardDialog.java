package com.kswkev.minesweeper.ui;

import com.kswkev.minesweeper.model.BoardConfig;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Optional;

/** Modal dialog for choosing a custom board size and mine count. */
final class CustomBoardDialog extends JDialog {

    private final SpinnerNumberModel rowsModel;
    private final SpinnerNumberModel colsModel;
    private final SpinnerNumberModel minesModel;

    private BoardConfig result;

    private CustomBoardDialog(JFrame owner, BoardConfig initial) {
        super(owner, "Custom Board", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        rowsModel = new SpinnerNumberModel(initial.rows(), BoardConfig.MIN_ROWS, BoardConfig.MAX_ROWS, 1);
        colsModel = new SpinnerNumberModel(initial.cols(), BoardConfig.MIN_COLS, BoardConfig.MAX_COLS, 1);
        minesModel = new SpinnerNumberModel(initial.mines(), BoardConfig.MIN_MINES,
                BoardConfig.maxMines(initial.rows(), initial.cols()), 1);
        rowsModel.addChangeListener(e -> updateMaxMines());
        colsModel.addChangeListener(e -> updateMaxMines());

        JPanel fields = new JPanel(new GridLayout(3, 2, 8, 6));
        fields.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        fields.add(new JLabel("Rows:"));
        fields.add(new JSpinner(rowsModel));
        fields.add(new JLabel("Columns:"));
        fields.add(new JSpinner(colsModel));
        fields.add(new JLabel("Mines:"));
        fields.add(new JSpinner(minesModel));

        JButton ok = new JButton("OK");
        ok.addActionListener(e -> {
            result = new BoardConfig(value(rowsModel), value(colsModel), value(minesModel));
            dispose();
        });
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(ok);
        buttons.add(cancel);

        add(fields, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(ok);
        pack();
        setLocationRelativeTo(owner);
    }

    /** Shows the dialog and returns the chosen config, or empty if cancelled. */
    static Optional<BoardConfig> show(JFrame owner, BoardConfig initial) {
        CustomBoardDialog dialog = new CustomBoardDialog(owner, initial);
        dialog.setVisible(true);
        return Optional.ofNullable(dialog.result);
    }

    private void updateMaxMines() {
        int max = BoardConfig.maxMines(value(rowsModel), value(colsModel));
        minesModel.setMaximum(max);
        if (value(minesModel) > max) {
            minesModel.setValue(max);
        }
    }

    private static int value(SpinnerNumberModel model) {
        return model.getNumber().intValue();
    }
}
