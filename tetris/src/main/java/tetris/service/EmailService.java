package tetris.service;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class EmailService {

    
    private static final String DEFAULT_FROM     = "tetris.app7@gmail.com";
    private static final String DEFAULT_PASSWORD = "ctde ehuc wygl ldor";

    private static Properties chargerConfig() {
        Properties p = new Properties();

        
        try (InputStream in = EmailService.class.getResourceAsStream("/email.properties")) {
            if (in != null) {
                p.load(in);
                return p;
            }
        } catch (IOException e) {
            System.err.println("Erreur lecture email.properties (classpath) : " + e.getMessage());
        }

       
        for (Path configPath : getConfigCandidates()) {
            if (!Files.isRegularFile(configPath)) continue;
            try (InputStream in = Files.newInputStream(configPath)) {
                p.load(in);
                return p;
            } catch (IOException e) {
                System.err.println("Erreur lecture email.properties : " + e.getMessage());
            }
        }
        return null;
    }

    private static List<Path> getConfigCandidates() {
        Set<Path> candidates = new LinkedHashSet<>();
        candidates.add(Path.of("email.properties").toAbsolutePath().normalize());
        try {
            Path codePath = Path.of(EmailService.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
            Path appDir = Files.isRegularFile(codePath) ? codePath.getParent() : codePath;
            if (appDir != null) {
                candidates.add(appDir.resolve("email.properties").toAbsolutePath().normalize());
                Path parent = appDir.getParent();
                if (parent != null) {
                    candidates.add(parent.resolve("email.properties").toAbsolutePath().normalize());
                }
            }
        } catch (Exception ignored) {
        }
        return new ArrayList<>(candidates);
    }

    public static void envoyerConfirmation(String destinataire, String pseudo) {
        Properties config = chargerConfig();

        String expediteur;
        String motDePasse;
        if (config != null) {
            expediteur = config.getProperty("mail.from", "").trim();
            motDePasse = config.getProperty("mail.password", "").replaceAll("\\s+", "");
        } else {
            System.err.println("email.properties introuvable - utilisation de la configuration de secours.");
            expediteur = DEFAULT_FROM.trim();
            motDePasse = DEFAULT_PASSWORD.replaceAll("\\s+", "");
        }

        if (expediteur.isEmpty() || motDePasse.isEmpty()) {
            System.err.println("Identifiants email manquants.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth",             "true");
        props.put("mail.smtp.starttls.enable",  "true");
        props.put("mail.smtp.starttls.required","true");
        props.put("mail.smtp.host",             "smtp.gmail.com");
        props.put("mail.smtp.port",             "587");
        
        props.put("mail.smtp.ssl.protocols",    "TLSv1.2");
        props.put("mail.smtp.ssl.trust",        "smtp.gmail.com");

        final String exp = expediteur;
        final String pwd = motDePasse;
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(exp, pwd);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(expediteur, "Tetris Game"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            msg.setSubject("Bienvenue sur Tetris, " + pseudo + " !");
            msg.setText(
                "Bonjour " + pseudo + ",\n\n" +
                "Votre compte Tetris a ete cree avec succes !\n\n" +
                "Email : " + destinataire + "\n\n" +
                "Bonne partie !\n" +
                "L'equipe Tetris"
            );
            Transport.send(msg);
            System.out.println("Email de confirmation envoye a " + destinataire);
        } catch (Exception e) {
            System.err.println("Erreur envoi email : " + e.getMessage());
        }
    }
}
