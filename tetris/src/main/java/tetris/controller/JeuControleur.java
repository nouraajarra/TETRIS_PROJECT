package tetris.controller;

import tetris.database.DatabaseManager;
import tetris.model.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;

public class JeuControleur {

    public enum Etat { EN_COURS, PAUSE, GAME_OVER }

    private final Grille grille = new Grille();
    private final List<Piece> piecesCourantes = new ArrayList<>();
    private final List<Piece> piecesFantomes  = new ArrayList<>();
    private final Map<Piece, Integer> pieceSpawnX = new LinkedHashMap<>();
    private Piece pieceSuivante;
    private Score score;
    private int niveau;
    private int stage;
    private Etat etat;
    private Joueur joueur;
    private Instant debutPartie;

    private final javax.swing.Timer timer;
    private final Random random = new Random();
    private Consumer<Void> onUpdate;

    public JeuControleur() {
        score  = new Score();
        niveau = 1;
        stage  = 1;
        etat   = Etat.GAME_OVER;
        timer  = new javax.swing.Timer(Jeu.getIntervalle(niveau), this::tick);
    }

    
    public void setOnUpdate(Consumer<Void> callback) { this.onUpdate = callback; }
    public void setJoueur(Joueur j) { this.joueur = j; }

    public void nouvellePartie() {
        grille.reinitialiser();
        score.reset();
        niveau = 1;
        stage  = 1;
        etat   = Etat.EN_COURS;
        debutPartie = Instant.now();
        piecesCourantes.clear();
        piecesFantomes.clear();
        pieceSpawnX.clear();
        pieceSuivante = genererPiece();
        spawnToutesLesPieces();
        timer.setDelay(getIntervalleActuel());
        timer.restart();
        notifier();
    }

    public void togglePause() {
        if (etat == Etat.EN_COURS) {
            etat = Etat.PAUSE;
            timer.stop();
        } else if (etat == Etat.PAUSE) {
            etat = Etat.EN_COURS;
            timer.start();
        }
        notifier();
    }

    public void gererTouche(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_P, KeyEvent.VK_ESCAPE -> togglePause();
            case KeyEvent.VK_R -> nouvellePartie();
            default -> {
                if (etat != Etat.EN_COURS) return;
                switch (keyCode) {
                    case KeyEvent.VK_LEFT  -> deplacerGauche();
                    case KeyEvent.VK_RIGHT -> deplacerDroite();
                    case KeyEvent.VK_DOWN  -> softDrop();
                    case KeyEvent.VK_UP, KeyEvent.VK_X -> tournerHoraire();
                    case KeyEvent.VK_Z     -> tournerAntihoraire();
                    case KeyEvent.VK_SPACE -> hardDrop();
                }
            }
        }
    }

    
    private void deplacerGauche() {
        if (piecesCourantes.stream().allMatch(p -> peutDeplacer(p, "LEFT"))) {
            piecesCourantes.forEach(p -> p.deplacer("LEFT"));
            calculerFantomes();
            notifier();
        }
    }

    private void deplacerDroite() {
        if (piecesCourantes.stream().allMatch(p -> peutDeplacer(p, "RIGHT"))) {
            piecesCourantes.forEach(p -> p.deplacer("RIGHT"));
            calculerFantomes();
            notifier();
        }
    }

    private void softDrop() {
        List<Piece> aVerrouiller = new ArrayList<>();
        for (Piece p : new ArrayList<>(piecesCourantes)) {
            p.deplacer("DOWN");
            if (grille.verifierCollision(p)) {
                p.setPositionY(p.getPositionY() - 1);
                aVerrouiller.add(p);
            } else {
                score.ajouterPoints(1);
            }
        }
        for (Piece p : aVerrouiller) {
            verrouillerUnePiece(p);
            if (etat == Etat.GAME_OVER) return;
        }
        calculerFantomes();
        notifier();
    }

    private void hardDrop() {
        for (Piece p : new ArrayList<>(piecesCourantes)) {
            int dropped = 0;
            while (peutDeplacer(p, "DOWN")) {
                p.deplacer("DOWN");
                dropped++;
            }
            score.ajouterPoints(dropped * 2);
            verrouillerUnePiece(p);
            if (etat == Etat.GAME_OVER) return;
        }
    }

    private void tournerHoraire() {
        for (Piece p : piecesCourantes) appliquerRotation(p, true);
        calculerFantomes();
        notifier();
    }

    private void tournerAntihoraire() {
        for (Piece p : piecesCourantes) appliquerRotation(p, false);
        calculerFantomes();
        notifier();
    }

    private void appliquerRotation(Piece p, boolean horaire) {
        if (horaire) p.tourner(); else p.tournerInverse();
        if (grille.verifierCollision(p)) {
            p.deplacer("LEFT");
            if (grille.verifierCollision(p)) {
                p.deplacer("RIGHT");
                p.deplacer("RIGHT");
                if (grille.verifierCollision(p)) {
                    p.deplacer("LEFT");
                    if (horaire) p.tournerInverse(); else p.tourner();
                }
            }
        }
    }

    

    private void tick(ActionEvent e) {
        List<Piece> aVerrouiller = new ArrayList<>();
        for (Piece p : new ArrayList<>(piecesCourantes)) {
            p.deplacer("DOWN");
            if (grille.verifierCollision(p)) {
                p.setPositionY(p.getPositionY() - 1);
                aVerrouiller.add(p);
            }
        }
        for (Piece p : aVerrouiller) {
            verrouillerUnePiece(p);
            if (etat == Etat.GAME_OVER) return;
        }
        if (aVerrouiller.isEmpty()) notifier();
    }

    private void verrouillerUnePiece(Piece p) {
        grille.verouiller(p);
        int spawnX = pieceSpawnX.getOrDefault(p, Jeu.getSpawnPositions(stage)[0]);
        piecesCourantes.remove(p);
        pieceSpawnX.remove(p);

        int lignes = grille.verifierEtSupprimerLignes();
        if (lignes > 0) {
            score.calculer(lignes, niveau);
            mettreAJourStageEtNiveau();
        }

        if (grille.estGameOver()) {
            gameOver();
            return;
        }

        Piece nouveau = pieceSuivante;
        pieceSuivante = genererPiece();
        spawnPieceAt(nouveau, spawnX);
        calculerFantomes();
        notifier();
    }

    private void mettreAJourStageEtNiveau() {
        int nouvStage = Jeu.getStage(grille.getNbLignes());
        if (nouvStage != stage) {
            stage = nouvStage;
            ajusterNombrePieces();
            timer.setDelay(getIntervalleActuel());
        }
        int nouveauNiveau = Jeu.calculerNiveau(grille.getNbLignes());
        if (nouveauNiveau != niveau) {
            niveau = nouveauNiveau;
            timer.setDelay(getIntervalleActuel());
        }
    }

    private void ajusterNombrePieces() {
        Set<Integer> used = new HashSet<>(pieceSpawnX.values());
        for (int x : Jeu.getSpawnPositions(stage)) {
            if (!used.contains(x)) {
                Piece p = genererPiece();
                p.setPositionX(x);
                p.setPositionY(0);
                if (!grille.verifierCollision(p)) {
                    piecesCourantes.add(p);
                    pieceSpawnX.put(p, x);
                    used.add(x);
                }
            }
        }
    }

    private void spawnToutesLesPieces() {
        int[] positions = Jeu.getSpawnPositions(stage);
        for (int i = 0; i < positions.length; i++) {
            Piece p = (i == 0) ? pieceSuivante : genererPiece();
            if (i == 0) pieceSuivante = genererPiece();
            spawnPieceAt(p, positions[i]);
            if (etat == Etat.GAME_OVER) return;
        }
        calculerFantomes();
    }

    private void spawnPieceAt(Piece p, int x) {
        p.setPositionX(x);
        p.setPositionY(0);
        if (grille.verifierCollision(p)) {
            if (etat != Etat.GAME_OVER) gameOver();
        } else {
            piecesCourantes.add(p);
            pieceSpawnX.put(p, x);
        }
    }

    private void gameOver() {
        etat = Etat.GAME_OVER;
        timer.stop();
        sauvegarderPartie();
        notifier();
    }

    private void sauvegarderPartie() {
        if (joueur == null) return;
        long duree = Instant.now().getEpochSecond() - debutPartie.getEpochSecond();
        Partie partie = new Partie(joueur.getIdJoueur());
        partie.setDateDebut(LocalDateTime.ofInstant(debutPartie, ZoneId.systemDefault()));
        partie.terminer(duree);
        partie.setNiveau(niveau);
        partie.setScore(score.getValeur());
        partie.setLignesEffacees(score.getLignesEffacees());
        boolean sauvegardee = DatabaseManager.getInstance().sauvegarderPartie(partie);
        if (sauvegardee) {
            Historique h = DatabaseManager.getInstance().chargerHistorique(joueur.getIdJoueur());
            joueur.setHistorique(h);
        } else {
            System.err.println("Erreur : la partie n'a pas pu être sauvegardée.");
        }
    }

    private void calculerFantomes() {
        piecesFantomes.clear();
        for (Piece p : piecesCourantes) {
            Piece fantome = p.copie();
            if (fantome == null) continue;
            while (true) {
                fantome.deplacer("DOWN");
                if (grille.verifierCollision(fantome)) {
                    fantome.setPositionY(fantome.getPositionY() - 1);
                    break;
                }
            }
            piecesFantomes.add(fantome);
        }
    }

    private boolean peutDeplacer(Piece p, String dir) {
        p.deplacer(dir);
        boolean ok = !grille.verifierCollision(p);
        switch (dir) {
            case "LEFT"  -> p.deplacer("RIGHT");
            case "RIGHT" -> p.deplacer("LEFT");
            case "DOWN"  -> p.setPositionY(p.getPositionY() - 1);
        }
        return ok;
    }

    private Piece genererPiece() {
        Piece[] types = {
            new PieceI(), new PieceO(), new PieceT(),
            new PieceS(), new PieceZ(), new PieceL(), new PieceJ()
        };
        return types[random.nextInt(types.length)];
    }

    private int getIntervalleActuel() {
        int base = Jeu.getIntervalle(niveau);
        return stage == 2 ? Math.max(50, base / 2) : base;
    }

    private void notifier() {
        if (onUpdate != null) onUpdate.accept(null);
    }

    
    public Grille getGrille() { return grille; }
    public List<Piece> getPiecesCourantes() { return piecesCourantes; }
    public List<Piece> getPiecesFantomes()  { return piecesFantomes; }
    public Piece getPieceSuivante() { return pieceSuivante; }
    public Score getScore()  { return score; }
    public int getNiveau()   { return niveau; }
    public int getStage()    { return stage; }
    public Etat getEtat()    { return etat; }
    public int getMeilleurScore() {
        if (joueur != null) return DatabaseManager.getInstance().getMeilleurScoreJoueur(joueur.getIdJoueur());
        return DatabaseManager.getInstance().getMeilleurScoreGlobal();
    }
}
