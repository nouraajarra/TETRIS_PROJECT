package tetris.model;

import java.time.LocalDateTime;

public class Partie {
    private int idPartie;
    private int idJoueur;
    private LocalDateTime dateDebut;
    private long duree; // seconds
    private int niveau;
    private int score;
    private int lignesEffacees;
    private boolean enPause;
    private boolean sauvegardee = false;

    public Partie(int idJoueur) {
        this.idJoueur = idJoueur;
        this.dateDebut = LocalDateTime.now();
        this.niveau = 1;
        this.score = 0;
        this.lignesEffacees = 0;
        this.enPause = false;
    }

    public void mettreEnPause() { this.enPause = !this.enPause; }
    public void terminer(long dureeSecondes) { this.duree = dureeSecondes; }

    public int getIdPartie() { return idPartie; }
    public void setIdPartie(int id) { this.idPartie = id; }
    public int getIdJoueur() { return idJoueur; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public long getDuree() { return duree; }
    public void setDuree(long duree) { this.duree = duree; }
    public int getNiveau() { return niveau; }
    public void setNiveau(int n) { this.niveau = n; }
    public int getScore() { return score; }
    public void setScore(int s) { this.score = s; }
    public int getLignesEffacees() { return lignesEffacees; }
    public void setLignesEffacees(int l) { this.lignesEffacees = l; }
    public boolean isEnPause() { return enPause; }
    public boolean isSauvegardee() { return sauvegardee; }
    public void setSauvegardee(boolean b) { this.sauvegardee = b; }
}
