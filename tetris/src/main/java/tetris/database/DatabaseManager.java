package tetris.database;

import tetris.model.Historique;
import tetris.model.Joueur;
import tetris.model.Partie;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:tetris.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public void initialiser() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("DB init error: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        String joueur = """
            CREATE TABLE IF NOT EXISTS joueur (
                id_joueur   INTEGER PRIMARY KEY AUTOINCREMENT,
                pseudo      TEXT NOT NULL UNIQUE,
                email       TEXT NOT NULL UNIQUE,
                mot_de_passe TEXT NOT NULL
            )""";
        String historique = """
            CREATE TABLE IF NOT EXISTS historique (
                id_historique     INTEGER PRIMARY KEY AUTOINCREMENT,
                id_joueur         INTEGER NOT NULL,
                date_creation     TEXT NOT NULL,
                nb_parties_jouees INTEGER DEFAULT 0,
                meilleur_score    INTEGER DEFAULT 0,
                FOREIGN KEY(id_joueur) REFERENCES joueur(id_joueur)
            )""";
        String partie = """
            CREATE TABLE IF NOT EXISTS partie (
                id_partie       INTEGER PRIMARY KEY AUTOINCREMENT,
                id_joueur       INTEGER NOT NULL,
                date_debut      TEXT NOT NULL,
                duree           INTEGER DEFAULT 0,
                niveau          INTEGER DEFAULT 1,
                score           INTEGER DEFAULT 0,
                lignes_effacees INTEGER DEFAULT 0,
                FOREIGN KEY(id_joueur) REFERENCES joueur(id_joueur)
            )""";
        try (Statement st = connection.createStatement()) {
            st.execute(joueur);
            st.execute(historique);
            st.execute(partie);
        }
    }

    // ── Joueur ──────────────────────────────────────────────────────────────

    public Joueur inscrireJoueur(String pseudo, String email, String motDePasse) {
        String sql = "INSERT INTO joueur(pseudo, email, mot_de_passe) VALUES(?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, pseudo);
            ps.setString(2, email);
            ps.setString(3, motDePasse);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (Statement st = connection.createStatement();
                     ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        Joueur j = new Joueur(pseudo, email, motDePasse);
                        j.setIdJoueur(rs.getInt(1));
                        creerHistorique(j.getIdJoueur());
                        return j;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Inscription error: " + e.getMessage());
        }
        return null;
    }

    public Joueur connecterJoueur(String email, String motDePasse) {
        String sql = "SELECT * FROM joueur WHERE email=? AND mot_de_passe=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, motDePasse);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Joueur j = new Joueur(rs.getString("pseudo"), email, motDePasse);
                j.setIdJoueur(rs.getInt("id_joueur"));
                Historique h = chargerHistorique(j.getIdJoueur());
                j.setHistorique(h);
                return j;
            }
        } catch (SQLException e) {
            System.err.println("Connexion error: " + e.getMessage());
        }
        return null;
    }

    public boolean pseudoExiste(String pseudo) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT id_joueur FROM joueur WHERE pseudo=?")) {
            ps.setString(1, pseudo);
            return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public boolean emailExiste(String email) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT id_joueur FROM joueur WHERE email=?")) {
            ps.setString(1, email);
            return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    // ── Historique ──────────────────────────────────────────────────────────

    private int creerHistorique(int idJoueur) {
        String sql = "INSERT INTO historique(id_joueur, date_creation) VALUES(?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idJoueur);
            ps.setString(2, LocalDate.now().toString());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (Statement st = connection.createStatement();
                     ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Historique create error: " + e.getMessage());
        }
        return 0;
    }

    private int assurerHistorique(int idJoueur) {
        String sql = "SELECT id_historique FROM historique WHERE id_joueur=? ORDER BY id_historique LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idJoueur);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_historique");
        } catch (SQLException e) {
            System.err.println("Historique lookup error: " + e.getMessage());
        }
        return creerHistorique(idJoueur);
    }

    public Historique chargerHistorique(int idJoueur) {
        Historique h = new Historique(idJoueur);
        List<Partie> parties = chargerParties(idJoueur);
        int meilleurScore = parties.stream()
            .mapToInt(Partie::getScore)
            .max()
            .orElse(0);

        h.setIdHistorique(assurerHistorique(idJoueur));
        h.setParties(parties);
        h.setNbPartiesJouees(parties.size());
        h.setMeilleurScore(meilleurScore);
        synchroniserHistorique(idJoueur, parties.size(), meilleurScore);
        return h;
    }

    private void synchroniserHistorique(int idJoueur, int nbParties, int meilleurScore) {
        int idHistorique = assurerHistorique(idJoueur);
        if (idHistorique == 0) return;

        String sql = """
            UPDATE historique
            SET nb_parties_jouees = ?,
                meilleur_score = ?
            WHERE id_historique = ?""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, nbParties);
            ps.setInt(2, meilleurScore);
            ps.setInt(3, idHistorique);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Historique update error: " + e.getMessage());
        }
    }

    private void majHistorique(int idJoueur) {
        String sql = "SELECT COUNT(*) AS nb, COALESCE(MAX(score), 0) AS meilleur FROM partie WHERE id_joueur=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idJoueur);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                synchroniserHistorique(idJoueur, rs.getInt("nb"), rs.getInt("meilleur"));
            }
        } catch (SQLException e) {
            System.err.println("Historique aggregate error: " + e.getMessage());
        }
    }

    // ── Partie ──────────────────────────────────────────────────────────────

    public boolean sauvegarderPartie(Partie p) {
        String sql = """
            INSERT INTO partie(id_joueur, date_debut, duree, niveau, score, lignes_effacees)
            VALUES(?,?,?,?,?,?)""";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, p.getIdJoueur());
            ps.setString(2, p.getDateDebut().toString());
            ps.setLong(3, p.getDuree());
            ps.setInt(4, p.getNiveau());
            ps.setInt(5, p.getScore());
            ps.setInt(6, p.getLignesEffacees());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (Statement st = connection.createStatement();
                     ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) p.setIdPartie(rs.getInt(1));
                }
                majHistorique(p.getIdJoueur());
                p.setSauvegardee(true);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Partie save error: " + e.getMessage());
        }
        p.setSauvegardee(false);
        return false;
    }

    public List<Partie> chargerParties(int idJoueur) {
        List<Partie> parties = new ArrayList<>();
        String sql = "SELECT * FROM partie WHERE id_joueur=? ORDER BY date_debut DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idJoueur);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Partie p = new Partie(idJoueur);
                p.setIdPartie(rs.getInt("id_partie"));
                p.setDateDebut(LocalDateTime.parse(rs.getString("date_debut")));
                p.setDuree(rs.getLong("duree"));
                p.setNiveau(rs.getInt("niveau"));
                p.setScore(rs.getInt("score"));
                p.setLignesEffacees(rs.getInt("lignes_effacees"));
                parties.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Parties load error: " + e.getMessage());
        }
        return parties;
    }

    public int getMeilleurScoreGlobal() {
        try (Statement st = connection.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT MAX(score) FROM partie");
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { /* ignore */ }
        return 0;
    }

    public int getMeilleurScoreJoueur(int idJoueur) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT MAX(score) FROM partie WHERE id_joueur=?")) {
            ps.setInt(1, idJoueur);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { /* ignore */ }
        return 0;
    }

    // ── Statistiques ────────────────────────────────────────────────────────

    public double getScoreMoyen(int idJoueur) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT AVG(score) FROM partie WHERE id_joueur=?")) {
            ps.setInt(1, idJoueur);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { /* ignore */ }
        return 0;
    }

    public double getLignesMoyennes(int idJoueur) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT AVG(lignes_effacees) FROM partie WHERE id_joueur=?")) {
            ps.setInt(1, idJoueur);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { /* ignore */ }
        return 0;
    }

    public void fermer() {
        try { if (connection != null) connection.close(); }
        catch (SQLException e) { /* ignore */ }
    }
}
