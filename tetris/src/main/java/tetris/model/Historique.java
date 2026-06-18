package tetris.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Historique {
    private int idHistorique;
    private int idJoueur;
    private LocalDate dateCreation;
    private int nbPartiesJouees;
    private int meilleurScore;
    private List<Partie> parties;

    public Historique(int idJoueur) {
        this.idJoueur = idJoueur;
        this.dateCreation = LocalDate.now();
        this.nbPartiesJouees = 0;
        this.meilleurScore = 0;
        this.parties = new ArrayList<>();
    }

    public void ajouterPartie(Partie p) {
        parties.add(p);
        nbPartiesJouees++;
        if (p.getScore() > meilleurScore) meilleurScore = p.getScore();
    }

    public void supprimerPartie(int id) {
        parties.removeIf(p -> p.getIdPartie() == id);
    }

    public int getMeilleurScore() { return meilleurScore; }

    public List<Partie> filtrerParDate(LocalDate d) {
        List<Partie> result = new ArrayList<>();
        for (Partie p : parties) {
            if (p.getDateDebut().toLocalDate().equals(d)) result.add(p);
        }
        return result;
    }

    public void afficher() {
        System.out.println("Historique — Parties: " + nbPartiesJouees + " | Meilleur score: " + meilleurScore);
    }

    public int getIdHistorique() { return idHistorique; }
    public void setIdHistorique(int id) { this.idHistorique = id; }
    public int getIdJoueur() { return idJoueur; }
    public int getNbPartiesJouees() { return nbPartiesJouees; }
    public void setNbPartiesJouees(int n) { this.nbPartiesJouees = n; }
    public void setMeilleurScore(int s) { this.meilleurScore = s; }
    public List<Partie> getParties() { return parties; }
    public void setParties(List<Partie> p) { this.parties = p; }
}
