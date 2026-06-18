package tetris.model;

import java.awt.Color;

public class Case {
    private int x;
    private int y;
    private Color couleur;

    public Case(int x, int y) {
        this.x = x;
        this.y = y;
        this.couleur = null;
    }

    public boolean estOccupee() { return couleur != null; }
    public int getX() { return x; }
    public int getY() { return y; }
    public Color getCouleur() { return couleur; }
    public void setCouleur(Color couleur) { this.couleur = couleur; }
    public void vider() { this.couleur = null; }
}
