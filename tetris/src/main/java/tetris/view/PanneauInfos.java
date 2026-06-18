package tetris.view;

import tetris.controller.JeuControleur;
import tetris.model.Grille;
import tetris.model.Piece;

import javax.swing.*;
import java.awt.*;

public class PanneauInfos extends JPanel {

    private final JeuControleur ctrl;

    private final JLabel lblScore        = makeValue("0");
    private final JLabel lblMeilleur     = makeValue("0");
    private final JLabel lblNiveau       = makeValue("1");
    private final JLabel lblLignes       = makeValue("0");
    private final JLabel lblStage        = makeValue("1");
    private final NextPiecePanel nextPanel;

    public PanneauInfos(JeuControleur ctrl) {
        this.ctrl  = ctrl;
        this.nextPanel = new NextPiecePanel(ctrl);
        setBackground(new Color(20, 20, 20));
        setPreferredSize(new Dimension(160, tetris.model.Grille.HAUTEUR * JeuVue.getCellSize()));
        buildUI();
    }

    private void buildUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 2, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.gridx  = 0; gc.gridy = 0;

        addSection("TETRIS", null, gc);
        addSection("Score",     lblScore,    gc);
        addSection("Meilleur",  lblMeilleur, gc);
        addSection("Niveau",    lblNiveau,   gc);
        addSection("Lignes",    lblLignes,   gc);
        addSection("Stage",     lblStage,    gc);

        gc.gridy++;
        gc.insets = new Insets(12, 8, 2, 8);
        add(makeLabel("Suivante"), gc);
        gc.gridy++;
        gc.insets = new Insets(2, 8, 2, 8);
        add(nextPanel, gc);

        // Keyboard hints
        gc.gridy++;
        gc.insets = new Insets(20, 8, 2, 8);
        add(makeLabel("Contrôles"), gc);
        gc.gridy++;
        gc.insets = new Insets(2, 8, 2, 8);
        add(makeHint("← → ↓  Déplacer"), gc);
        gc.gridy++;
        add(makeHint("↑ / X   Tourner"), gc);
        gc.gridy++;
        add(makeHint("Espace  Hard drop"), gc);
        gc.gridy++;
        add(makeHint("P / Esc Pause"), gc);
        gc.gridy++;
        add(makeHint("R       Restart"), gc);

        // Filler
        gc.gridy++;
        gc.weighty = 1.0;
        add(Box.createVerticalGlue(), gc);
    }

    private void addSection(String title, JLabel value, GridBagConstraints gc) {
        add(makeLabel(title), gc);
        gc.gridy++;
        if (value != null) {
            gc.insets = new Insets(0, 8, 6, 8);
            add(value, gc);
            gc.gridy++;
            gc.insets = new Insets(6, 8, 2, 8);
        }
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.GRAY);
        l.setFont(new Font("Arial", Font.BOLD, 11));
        return l;
    }

    private static JLabel makeValue(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Arial", Font.BOLD, 20));
        return l;
    }

    private JLabel makeHint(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(100, 100, 100));
        l.setFont(new Font("Monospaced", Font.PLAIN, 10));
        return l;
    }

    public void rafraichir() {
        lblScore.setText(String.valueOf(ctrl.getScore().getValeur()));
        lblMeilleur.setText(String.valueOf(ctrl.getMeilleurScore()));
        lblNiveau.setText(String.valueOf(ctrl.getNiveau()));
        lblLignes.setText(String.valueOf(ctrl.getScore().getLignesEffacees()));
        lblStage.setText(String.valueOf(ctrl.getStage()));
        nextPanel.repaint();
    }

    // ── Inner panel for next-piece preview ───────────────────────────────────

    private static class NextPiecePanel extends JPanel {
        private static final int CELL = 22;
        private final JeuControleur ctrl;

        NextPiecePanel(JeuControleur c) {
            this.ctrl = c;
            setPreferredSize(new Dimension(5 * CELL, 5 * CELL));
            setBackground(new Color(30, 30, 30));
            setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Piece p = ctrl.getPieceSuivante();
            if (p == null) return;
            int[][] forme = p.getForme();
            int cols = forme[0].length;
            int rows = forme.length;
            int offsetX = (getWidth()  - cols * CELL) / 2;
            int offsetY = (getHeight() - rows * CELL) / 2;
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    if (forme[row][col] != 0) {
                        int x = offsetX + col * CELL + 1;
                        int y = offsetY + row * CELL + 1;
                        int s = CELL - 2;
                        g.setColor(p.getCouleurObj());
                        ((Graphics2D) g).fillRoundRect(x, y, s, s, 4, 4);
                        g.setColor(p.getCouleurObj().brighter());
                        g.drawRoundRect(x, y, s, s, 4, 4);
                    }
                }
            }
        }
    }

}
