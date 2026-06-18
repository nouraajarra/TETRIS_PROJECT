package tetris.view;

import tetris.controller.JeuControleur;
import tetris.model.Grille;
import tetris.model.Piece;

import javax.swing.*;
import java.awt.*;

public class JeuVue extends JPanel {

    private static final int CELL = 30;
    private final JeuControleur ctrl;

    public JeuVue(JeuControleur ctrl) {
        this.ctrl = ctrl;
        setPreferredSize(new Dimension(Grille.LARGEUR * CELL, Grille.HAUTEUR * CELL));
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);
        drawLockedCells(g2);
        drawGhostPiece(g2);
        drawCurrentPiece(g2);
        drawOverlay(g2);
    }

    private void drawGrid(Graphics2D g) {
        g.setColor(new Color(30, 30, 30));
        for (int col = 0; col <= Grille.LARGEUR; col++)
            g.drawLine(col * CELL, 0, col * CELL, Grille.HAUTEUR * CELL);
        for (int row = 0; row <= Grille.HAUTEUR; row++)
            g.drawLine(0, row * CELL, Grille.LARGEUR * CELL, row * CELL);
    }

    private void drawLockedCells(Graphics2D g) {
        Color[][] cases = ctrl.getGrille().getCases();
        for (int row = 0; row < Grille.HAUTEUR; row++) {
            for (int col = 0; col < Grille.LARGEUR; col++) {
                if (cases[row][col] != null) {
                    drawCell(g, col, row, cases[row][col], 1.0f);
                }
            }
        }
    }

    private void drawGhostPiece(Graphics2D g) {
        for (Piece ghost : ctrl.getPiecesFantomes()) {
            if (ghost == null) continue;
            int[][] forme = ghost.getForme();
            Color c = ghost.getCouleurObj();
            for (int row = 0; row < forme.length; row++) {
                for (int col = 0; col < forme[row].length; col++) {
                    if (forme[row][col] != 0) {
                        int x = ghost.getPositionX() + col;
                        int y = ghost.getPositionY() + row;
                        if (y >= 0) drawCell(g, x, y, c, 0.25f);
                    }
                }
            }
        }
    }

    private void drawCurrentPiece(Graphics2D g) {
        for (Piece p : ctrl.getPiecesCourantes()) {
            if (p == null) continue;
            int[][] forme = p.getForme();
            for (int row = 0; row < forme.length; row++) {
                for (int col = 0; col < forme[row].length; col++) {
                    if (forme[row][col] != 0) {
                        int x = p.getPositionX() + col;
                        int y = p.getPositionY() + row;
                        if (y >= 0) drawCell(g, x, y, p.getCouleurObj(), 1.0f);
                    }
                }
            }
        }
    }

    private void drawCell(Graphics2D g, int col, int row, Color c, float alpha) {
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        int x = col * CELL + 1;
        int y = row * CELL + 1;
        int s = CELL - 2;
        g.setColor(c);
        g.fillRoundRect(x, y, s, s, 6, 6);
        g.setColor(c.brighter());
        g.drawRoundRect(x, y, s, s, 6, 6);
        g.setComposite(old);
    }

    private void drawOverlay(Graphics2D g) {
        JeuControleur.Etat etat = ctrl.getEtat();
        if (etat == JeuControleur.Etat.PAUSE) {
            drawMessage(g, "PAUSE", "Appuyer sur P pour reprendre");
        } else if (etat == JeuControleur.Etat.GAME_OVER) {
            drawMessage(g, "GAME OVER", "Appuyer sur R pour rejouer");
        }
    }

    private void drawMessage(Graphics2D g, String title, String sub) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, getHeight() / 2 - 20);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        fm = g.getFontMetrics();
        g.setColor(Color.LIGHT_GRAY);
        g.drawString(sub, (getWidth() - fm.stringWidth(sub)) / 2, getHeight() / 2 + 15);
    }

    public static int getCellSize() { return CELL; }
}
