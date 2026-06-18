package tetris.view;

import tetris.controller.JeuControleur;
import tetris.model.Joueur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    private static final String USER_GUIDE = """
        TETRIS GAME - USER GUIDE

        1. Account
        - Create an account with a pseudo, email address, and password.
        - The password must contain at least 8 characters, one uppercase letter,
          one lowercase letter, and one number.
        - After registration, the app sends a confirmation email.
        - Use the login screen to connect with your email and password.

        2. Game Controls
        - Left Arrow: move the piece left
        - Right Arrow: move the piece right
        - Down Arrow: soft drop
        - Up Arrow or X: rotate clockwise
        - Z: rotate counter-clockwise
        - Space: hard drop
        - P or Escape: pause or resume
        - R: restart the game

        3. Stages
        - Stage 1: normal Tetris gameplay
        - Stage 2: faster falling speed
        - Stage 3: two active pieces
        - Stage 4: four active pieces

        4. Score
        - Clearing lines gives points.
        - Soft drop and hard drop also add points.
        - Line clear points are multiplied by the current level.

        5. History
        - When the game is over, the score is saved.
        - The history screen shows played games, best score, average score,
          and average cleared lines.

        6. Email Configuration
        - The app uses email.properties for Gmail confirmation emails.
        - Use a Gmail app password, not your normal Gmail password.
        - Keep your real email.properties private.
        """;

    private final JeuControleur ctrl = new JeuControleur();
    private JeuVue jeuVue;
    private PanneauInfos panneauInfos;
    private Joueur joueurCourant;

    private static final String CARD_LOGIN = "login";
    private static final String CARD_JEU   = "jeu";
    private static final String CARD_HISTO = "historique";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    public MainFrame() {
        setTitle("Tetris");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                tetris.database.DatabaseManager.getInstance().fermer();
            }
        });

        // Login screen
        LoginVue loginVue = new LoginVue(this::onLoginSuccess);
        cardPanel.add(loginVue, CARD_LOGIN);

        // Game screen (built once, reused)
        JPanel jeuPanel = buildJeuPanel();
        cardPanel.add(jeuPanel, CARD_JEU);

        setContentPane(cardPanel);
        cardLayout.show(cardPanel, CARD_LOGIN);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildJeuPanel() {
        jeuVue       = new JeuVue(ctrl);
        panneauInfos = new PanneauInfos(ctrl);

        ctrl.setOnUpdate(v -> SwingUtilities.invokeLater(() -> {
            jeuVue.repaint();
            panneauInfos.rafraichir();
        }));

        JPanel panel = new JPanel(new BorderLayout(4, 0));
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(jeuVue, BorderLayout.CENTER);
        panel.add(panneauInfos, BorderLayout.EAST);

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setOpaque(true);
        menuBar.setBackground(new Color(8, 75, 84));
        menuBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 220, 220)),
            BorderFactory.createEmptyBorder(3, 6, 3, 6)));

        JMenu menuJeu = new JMenu("Jeu");
        styleTopMenu(menuJeu);

        JMenuItem miNouvelle = item("Nouvelle partie", KeyEvent.VK_N);
        miNouvelle.addActionListener(e -> ctrl.nouvellePartie());

        JMenuItem miPause = item("Pause / Reprendre", KeyEvent.VK_P);
        miPause.addActionListener(e -> ctrl.togglePause());

        JMenuItem miHistorique = item("Historique", KeyEvent.VK_H);
        miHistorique.addActionListener(e -> ouvrirHistorique());

        JMenuItem miDeconnexion = item("Deconnexion", -1);
        miDeconnexion.addActionListener(e -> deconnecter());

        JMenuItem miQuitter = item("Quitter", KeyEvent.VK_Q);
        miQuitter.addActionListener(e -> System.exit(0));

        menuJeu.add(miNouvelle);
        menuJeu.add(miPause);
        menuJeu.addSeparator();
        menuJeu.add(miHistorique);
        menuJeu.addSeparator();
        menuJeu.add(miDeconnexion);
        menuJeu.add(miQuitter);
        menuBar.add(menuJeu);

        JMenu menuAide = new JMenu("Help");
        styleTopMenu(menuAide);

        JMenuItem miGuide = item("User Guide", KeyEvent.VK_G);
        miGuide.addActionListener(e -> ouvrirGuideUtilisateur());
        menuAide.add(miGuide);
        menuBar.add(menuAide);

        setJMenuBar(menuBar);

        
        panel.setFocusable(true);
        panel.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { ctrl.gererTouche(e.getKeyCode()); }
        });

        return panel;
    }

    private void styleTopMenu(JMenu menu) {
        menu.setOpaque(true);
        menu.setBackground(new Color(8, 75, 84));
        menu.setForeground(Color.WHITE);
        menu.setFont(new Font("Arial", Font.BOLD, 13));
        menu.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
    }

    private JMenuItem item(String text, int mnemonic) {
        JMenuItem mi = new JMenuItem(text);
        if (mnemonic > 0) mi.setMnemonic(mnemonic);
        mi.setBackground(new Color(30, 30, 35));
        mi.setForeground(Color.LIGHT_GRAY);
        mi.setOpaque(true);
        return mi;
    }

    
    private void onLoginSuccess(Joueur joueur) {
        this.joueurCourant = joueur;
        ctrl.setJoueur(joueur);
        cardLayout.show(cardPanel, CARD_JEU);
        pack();
        setLocationRelativeTo(null);
        // Focus the game panel for key events
        SwingUtilities.invokeLater(() -> {
            Component jeuPanel = cardPanel.getComponent(1);
            jeuPanel.requestFocusInWindow();
            ctrl.nouvellePartie();
        });
    }

    private void ouvrirHistorique() {
        if (joueurCourant == null) return;
        // Pause if playing
        if (ctrl.getEtat() == JeuControleur.Etat.EN_COURS) ctrl.togglePause();

        HistoriqueVue hv = new HistoriqueVue(joueurCourant, () -> {
            cardPanel.remove(cardPanel.getComponentCount() - 1);
            cardLayout.show(cardPanel, CARD_JEU);
            pack();
            setLocationRelativeTo(null);
            SwingUtilities.invokeLater(() -> cardPanel.getComponent(1).requestFocusInWindow());
        });

        cardPanel.add(hv, CARD_HISTO);
        cardLayout.show(cardPanel, CARD_HISTO);
        pack();
        setLocationRelativeTo(null);
    }

    private void ouvrirGuideUtilisateur() {
        if (ctrl.getEtat() == JeuControleur.Etat.EN_COURS) ctrl.togglePause();

        JDialog dlg = new JDialog(this, "User Guide", false);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JTextArea text = new JTextArea(USER_GUIDE);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setCaretPosition(0);
        text.setBackground(new Color(20, 20, 25));
        text.setForeground(Color.WHITE);
        text.setFont(new Font("Monospaced", Font.PLAIN, 13));
        text.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(text);
        scroll.setPreferredSize(new Dimension(560, 480));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 70)));

        JButton close = itemButton("Close");
        close.addActionListener(e -> dlg.dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(new Color(15, 15, 15));
        bottom.add(close);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(15, 15, 15));
        root.add(scroll, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JButton itemButton(String text) {
        JButton b = new JButton(text);
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        b.setBackground(Color.BLACK);
        b.setForeground(new Color(0, 220, 220));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 18, 6, 18)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void deconnecter() {
        joueurCourant = null;
        ctrl.setJoueur(null);
        // Rebuild login screen (fresh state)
        cardPanel.remove(0);
        LoginVue loginVue = new LoginVue(this::onLoginSuccess);
        cardPanel.add(loginVue, CARD_LOGIN, 0);
        cardLayout.show(cardPanel, CARD_LOGIN);
        setJMenuBar(null);
        pack();
        setLocationRelativeTo(null);
    }
}
