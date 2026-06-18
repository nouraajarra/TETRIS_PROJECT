package tetris;

import tetris.database.DatabaseManager;
import tetris.view.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        DatabaseManager.getInstance().initialiser();

        SwingUtilities.invokeLater(MainFrame::new);
    }
}
