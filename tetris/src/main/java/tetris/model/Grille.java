package tetris.model;

import java.awt.Color;

public class Grille {
    public static final int LARGEUR = 10;
    public static final int HAUTEUR = 20;

    private Color[][] cases;
    private int nbLignes;

    public Grille() {
        cases = new Color[HAUTEUR][LARGEUR];
        nbLignes = 0;
    }

    public void reinitialiser() {
        cases = new Color[HAUTEUR][LARGEUR];
        nbLignes = 0;
    }

    /** Returns true if the piece overlaps a wall, floor, or locked cell. */
    public boolean verifierCollision(Piece piece) {
        int[][] forme = piece.getForme();
        int px = piece.getPositionX();
        int py = piece.getPositionY();
        for (int row = 0; row < forme.length; row++) {
            for (int col = 0; col < forme[row].length; col++) {
                if (forme[row][col] != 0) {
                    int x = px + col;
                    int y = py + row;
                    if (x < 0 || x >= LARGEUR || y >= HAUTEUR) return true;
                    if (y >= 0 && cases[y][x] != null) return true;
                }
            }
        }
        return false;
    }

    /** Locks a piece into the grid. */
    public void verouiller(Piece piece) {
        int[][] forme = piece.getForme();
        int px = piece.getPositionX();
        int py = piece.getPositionY();
        Color couleur = piece.getCouleurObj();
        for (int row = 0; row < forme.length; row++) {
            for (int col = 0; col < forme[row].length; col++) {
                if (forme[row][col] != 0) {
                    int x = px + col;
                    int y = py + row;
                    if (y >= 0 && y < HAUTEUR && x >= 0 && x < LARGEUR) {
                        cases[y][x] = couleur;
                    }
                }
            }
        }
    }

    /** Clears complete lines and returns the count cleared. */
    public int verifierEtSupprimerLignes() {
        int cleared = 0;
        for (int row = HAUTEUR - 1; row >= 0; row--) {
            if (estLignePleine(row)) {
                supprimerLigne(row);
                row++; // recheck same index after shift
                cleared++;
            }
        }
        nbLignes += cleared;
        return cleared;
    }

    private boolean estLignePleine(int row) {
        for (int col = 0; col < LARGEUR; col++) {
            if (cases[row][col] == null) return false;
        }
        return true;
    }

    private void supprimerLigne(int ligneIndex) {
        for (int row = ligneIndex; row > 0; row--) {
            cases[row] = cases[row - 1].clone();
        }
        cases[0] = new Color[LARGEUR];
    }

    /** Returns true when the top row has any filled cell. */
    public boolean estGameOver() {
        for (int col = 0; col < LARGEUR; col++) {
            if (cases[0][col] != null) return true;
        }
        return false;
    }

    public Color[][] getCases() { return cases; }
    public int getNbLignes() { return nbLignes; }
}
