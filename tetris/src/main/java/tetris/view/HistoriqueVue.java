package tetris.view;

import tetris.database.DatabaseManager;
import tetris.model.Historique;
import tetris.model.Joueur;
import tetris.model.Partie;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class HistoriqueVue extends JPanel {

    private final Joueur joueur;
    private final Runnable onRetour;

    public HistoriqueVue(Joueur joueur, Runnable onRetour) {
        this.joueur   = joueur;
        this.onRetour = onRetour;
        setBackground(new Color(15, 15, 15));
        setLayout(new BorderLayout(10, 10));
        build();
    }

    private void build() {
        // Title bar
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(20, 20, 25));
        top.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel title = new JLabel("Historique de " + joueur.getPseudo());
        title.setForeground(new Color(0, 220, 220));
        title.setFont(new Font("Arial", Font.BOLD, 20));
        top.add(title, BorderLayout.WEST);

        JButton btnRetour = new JButton("← Retour");
        btnRetour.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnRetour.setBackground(Color.BLACK);
        btnRetour.setForeground(new Color(0, 220, 220));
        btnRetour.setFocusPainted(false);
        btnRetour.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 200), 1),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        btnRetour.setOpaque(true);
        btnRetour.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRetour.addActionListener(e -> onRetour.run());
        top.add(btnRetour, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Stats panel
        Historique h = DatabaseManager.getInstance().chargerHistorique(joueur.getIdJoueur());
        double scoreMoyen  = DatabaseManager.getInstance().getScoreMoyen(joueur.getIdJoueur());
        double lignesMoy   = DatabaseManager.getInstance().getLignesMoyennes(joueur.getIdJoueur());

        JPanel stats = new JPanel(new GridLayout(2, 4, 20, 8));
        stats.setBackground(new Color(25, 25, 30));
        stats.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        addStat(stats, "Parties jouées",   String.valueOf(h.getNbPartiesJouees()));
        addStat(stats, "Meilleur score",   String.valueOf(h.getMeilleurScore()));
        addStat(stats, "Score moyen",      String.format("%.0f", scoreMoyen));
        addStat(stats, "Lignes moy./partie", String.format("%.1f", lignesMoy));
        add(stats, BorderLayout.CENTER);

        // Parties table
        List<Partie> parties = h.getParties();
        String[] cols = {"#", "Date", "Niveau", "Score", "Lignes", "Durée (s)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        int i = 1;
        for (Partie p : parties) {
            model.addRow(new Object[]{
                i++,
                p.getDateDebut().toLocalDate(),
                p.getNiveau(),
                p.getScore(),
                p.getLignesEffacees(),
                p.getDuree()
            });
        }
        JTable table = new JTable(model);
        table.setBackground(new Color(30, 30, 35));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(50, 50, 60));
        table.getTableHeader().setBackground(new Color(40, 40, 50));
        table.getTableHeader().setForeground(Color.LIGHT_GRAY);
        table.setRowHeight(24);
        table.setSelectionBackground(new Color(0, 100, 100));
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(30, 30, 35));
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 14));
        add(scroll, BorderLayout.SOUTH);
        // Give most space to the table
        scroll.setPreferredSize(new Dimension(600, 300));
    }

    private void addStat(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setForeground(Color.GRAY);
        lbl.setFont(new Font("Arial", Font.PLAIN, 11));
        panel.add(lbl);

        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setForeground(Color.WHITE);
        val.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(val);
    }
}
