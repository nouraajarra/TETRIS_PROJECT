package tetris.model;

public class Joueur {
    private int idJoueur;
    private String pseudo;
    private String email;
    private String motDePasse;
    private Historique historique;

    public Joueur(String pseudo, String email, String motDePasse) {
        this.pseudo = pseudo;
        this.email = email;
        this.motDePasse = motDePasse;
        this.historique = new Historique(idJoueur);
    }

    public boolean seConnecter(String email, String motDePasse) {
        return this.email.equals(email) && this.motDePasse.equals(motDePasse);
    }

    public Historique consulterHistorique() { return historique; }

    public int getIdJoueur() { return idJoueur; }
    public void setIdJoueur(int id) { this.idJoueur = id; this.historique = new Historique(id); }
    public String getPseudo() { return pseudo; }
    public String getEmail() { return email; }
    public String getMotDePasse() { return motDePasse; }
    public Historique getHistorique() { return historique; }
    public void setHistorique(Historique h) { this.historique = h; }
}
