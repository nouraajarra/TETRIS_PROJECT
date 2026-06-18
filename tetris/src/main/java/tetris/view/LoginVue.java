package tetris.view;

import tetris.database.DatabaseManager;
import tetris.model.Joueur;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class LoginVue extends JPanel {

    private final Consumer<Joueur> onSuccess;

    private final JTextField tfEmail  = new JTextField(18);
    private final JPasswordField pfPass = new JPasswordField(18);
    private final JLabel lblMsg       = new JLabel(" ");

    public LoginVue(Consumer<Joueur> onSuccess) {
        this.onSuccess = onSuccess;
        setBackground(new Color(15, 15, 15));
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 10, 6, 10);
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = 2; gc.gridx = 0; gc.gridy = 0;

        JLabel title = new JLabel("TETRIS", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 42));
        title.setForeground(new Color(0, 220, 220));
        add(title, gc);

        gc.gridy++;
        JLabel sub = new JLabel("Connexion", SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.BOLD, 18));
        sub.setForeground(Color.GRAY);
        add(sub, gc);

        gc.gridy++; gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        add(lbl("Email :"), gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.WEST;
        style(tfEmail);
        add(tfEmail, gc);

        gc.gridy++; gc.gridx = 0; gc.anchor = GridBagConstraints.EAST;
        add(lbl("Mot de passe :"), gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.WEST;
        style(pfPass);
        add(pfPass, gc);

        gc.gridy++; gc.gridx = 0; gc.gridwidth = 2; gc.anchor = GridBagConstraints.CENTER;
        lblMsg.setForeground(Color.RED);
        lblMsg.setFont(new Font("Arial", Font.PLAIN, 12));
        add(lblMsg, gc);

        gc.gridy++;
        JButton btnLogin = btn("Se connecter");
        btnLogin.addActionListener(e -> seConnecter());
        add(btnLogin, gc);

        gc.gridy++;
        JButton btnReg = btn("Créer un compte");
        btnReg.addActionListener(e -> ouvrirInscription());
        add(btnReg, gc);

        
        getRootPane(); 
        pfPass.addActionListener(e -> seConnecter());
        tfEmail.addActionListener(e -> pfPass.requestFocus());
    }

    private void seConnecter() {
        String email = tfEmail.getText().trim();
        String pass  = new String(pfPass.getPassword());
        if (email.isEmpty() || pass.isEmpty()) { lblMsg.setText("Champs obligatoires."); return; }
        Joueur j = DatabaseManager.getInstance().connecterJoueur(email, pass);
        if (j != null) onSuccess.accept(j);
        else lblMsg.setText("Email ou mot de passe incorrect.");
    }

    private void ouvrirInscription() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Inscription", true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setContentPane(new InscriptionPanel(joueur -> {
            dlg.dispose();
            onSuccess.accept(joueur);
        }));
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    
    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.LIGHT_GRAY);
        l.setFont(new Font("Arial", Font.PLAIN, 13));
        return l;
    }

    private void style(JTextField tf) {
        tf.setBackground(new Color(40, 40, 50));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 100)),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        tf.setFont(new Font("Arial", Font.PLAIN, 13));
    }

    private JButton btn(String text) {
        JButton b = new JButton(text);
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        b.setBackground(Color.BLACK);
        b.setForeground(new Color(0, 220, 220));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 200), 1),
            BorderFactory.createEmptyBorder(7, 20, 7, 20)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    
    private static class InscriptionPanel extends JPanel {
        private final JTextField tfPseudo = new JTextField(16);
        private final JTextField tfEmail  = new JTextField(16);
        private final JPasswordField pfPass = new JPasswordField(16);
        private final JPasswordField pfPass2 = new JPasswordField(16);
        private final JLabel lblMsg = new JLabel(" ");
        private final Consumer<Joueur> onDone;

        InscriptionPanel(Consumer<Joueur> onDone) {
            this.onDone = onDone;
            setBackground(new Color(20, 20, 25));
            setLayout(new GridBagLayout());
            build();
        }

        private void build() {
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(5, 8, 5, 8);
            gc.fill   = GridBagConstraints.HORIZONTAL;
            gc.gridwidth = 2; gc.gridx = 0; gc.gridy = 0;

            JLabel t = new JLabel("Créer un compte", SwingConstants.CENTER);
            t.setForeground(Color.WHITE);
            t.setFont(new Font("Arial", Font.BOLD, 16));
            add(t, gc);

            addRow(gc, "Pseudo :",        tfPseudo);
            addRow(gc, "Email :",         tfEmail);
            addRow(gc, "Mot de passe :",  pfPass);
            
            gc.gridy++; gc.gridwidth = 2; gc.gridx = 0;
            JLabel hint = new JLabel("≥ 8 caractères, 1 majuscule, 1 minuscule, 1 chiffre");
            hint.setForeground(new Color(120, 120, 120));
            hint.setFont(new Font("Arial", Font.ITALIC, 10));
            add(hint, gc);
            addRow(gc, "Confirmation :",  pfPass2);

            gc.gridy++; gc.gridwidth = 2;
            lblMsg.setForeground(Color.RED);
            add(lblMsg, gc);

            gc.gridy++;
            JButton btn = new JButton("S'inscrire");
            btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            btn.setBackground(Color.BLACK);
            btn.setForeground(new Color(0, 220, 220));
            btn.setFocusPainted(false);
            btn.setOpaque(true);
            btn.setContentAreaFilled(true);
            btn.setFont(new Font("Arial", Font.BOLD, 13));
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 200, 200), 1),
                BorderFactory.createEmptyBorder(7, 20, 7, 20)));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> inscrire());
            add(btn, gc);
        }

        private void addRow(GridBagConstraints gc, String label, JTextField field) {
            gc.gridy++; gc.gridwidth = 1; gc.gridx = 0; gc.anchor = GridBagConstraints.EAST;
            JLabel l = new JLabel(label);
            l.setForeground(Color.LIGHT_GRAY);
            add(l, gc);
            gc.gridx = 1; gc.anchor = GridBagConstraints.WEST;
            field.setPreferredSize(new Dimension(160, 26));
            add(field, gc);
        }

        private void inscrire() {
            String pseudo = tfPseudo.getText().trim();
            String email  = tfEmail.getText().trim();
            String pass   = new String(pfPass.getPassword());
            String pass2  = new String(pfPass2.getPassword());

            if (pseudo.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                lblMsg.setText("Tous les champs sont obligatoires."); return;
            }
            if (pseudo.length() < 3) {
                lblMsg.setText("Pseudo trop court (minimum 3 caractères)."); return;
            }
            if (!email.matches("^[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)+$")) {
                lblMsg.setText("Adresse email invalide."); return;
            }
            String passErr = validerMotDePasse(pass);
            if (passErr != null) { lblMsg.setText(passErr); return; }
            if (!pass.equals(pass2)) {
                lblMsg.setText("Les mots de passe ne correspondent pas."); return;
            }

            DatabaseManager db = DatabaseManager.getInstance();
            if (db.pseudoExiste(pseudo)) { lblMsg.setText("Pseudo déjà utilisé."); return; }
            if (db.emailExiste(email))   { lblMsg.setText("Email déjà utilisé."); return; }
            Joueur j = db.inscrireJoueur(pseudo, email, pass);
            if (j != null) {
                new Thread(() -> tetris.service.EmailService.envoyerConfirmation(email, pseudo)).start();
                onDone.accept(j);
            } else {
                lblMsg.setText("Erreur lors de l'inscription.");
            }
        }

       
        private String validerMotDePasse(String pass) {
            if (pass.length() < 8)
                return "Mot de passe trop court (minimum 8 caractères).";
            if (!pass.matches(".*[A-Z].*"))
                return "Mot de passe doit contenir au moins une majuscule.";
            if (!pass.matches(".*[a-z].*"))
                return "Mot de passe doit contenir au moins une minuscule.";
            if (!pass.matches(".*\\d.*"))
                return "Mot de passe doit contenir au moins un chiffre.";
            return null;
        }
    }
}
