package tetris.model;

public class Score {
    private int valeur;
    private int lignesEffacees;

    private static final int[] POINTS = {0, 100, 300, 500, 900};

    public Score() {
        valeur = 0;
        lignesEffacees = 0;
    }

    public void reset() {
        valeur = 0;
        lignesEffacees = 0;
    }

    public int calculer(int nbLignes, int niveau) {
        int points = 0;
        if (nbLignes >= 1 && nbLignes <= 4) {
            points = POINTS[nbLignes] * niveau;
        }
        valeur += points;
        lignesEffacees += nbLignes;
        return points;
    }

    public void ajouterPoints(int pts) {
        valeur += pts;
    }

    public int getValeur() { return valeur; }
    public int getLignesEffacees() { return lignesEffacees; }
    public void setValeur(int v) { this.valeur = v; }
    public void setLignesEffacees(int l) { this.lignesEffacees = l; }
}
