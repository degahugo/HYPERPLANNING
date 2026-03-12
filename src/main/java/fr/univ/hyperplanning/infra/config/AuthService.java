package fr.univ.hyperplanning.infra.config;

import fr.univ.hyperplanning.dao.UtilisateurDAO;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthService {

    private static final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    
    public static Utilisateur connecter(String username, String motDePasse) {
        Utilisateur utilisateur = utilisateurDAO.findByUsername(username);
        if (utilisateur == null) return null;

        if (!hasher(motDePasse).equals(utilisateur.getMotDePasseHash())) return null;

        utilisateurDAO.mettreAJourDerniereConnexion(utilisateur);
        SessionManager.ouvrir(utilisateur);
        return utilisateur;
    }

    
    public static Utilisateur inscrire(String nom, String prenom, String email, String motDePasse) {
        // Validation email
        if (!email.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Adresse e-mail invalide.");
        }
        if (utilisateurDAO.emailExiste(email)) {
            throw new IllegalArgumentException("Cette adresse e-mail est déjà utilisée.");
        }
        // Validation mot de passe (min 8 car.)
        /*if (motDePasse.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères.");
        }*/

        String hash = hasher(motDePasse);
        return utilisateurDAO.inscrire(nom.trim(), prenom.trim(), email.trim(), hash);
    }

    
    public static void deconnecter() {
        SessionManager.fermer();
    }

   
    public static Utilisateur verifierIdentiteReinitialisation(String email, String username) {
        Utilisateur u = utilisateurDAO.findByEmail(email);
        if (u == null) return null;
        if (!UtilisateurDAO.buildUsername(u).equals(username)) return null;
        return u;
    }

    
    public static void reinitialiserMotDePasse(Utilisateur utilisateur, String nouveauMotDePasse) {
        /*if (nouveauMotDePasse.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères.");
        }*/
        utilisateurDAO.changerMotDePasse(utilisateur, hasher(nouveauMotDePasse));
    }

    
    public static String hasher(String motDePasse) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(motDePasse.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 non disponible", e);
        }
    }
}

