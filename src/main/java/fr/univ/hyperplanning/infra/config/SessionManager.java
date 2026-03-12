package fr.univ.hyperplanning.infra.config;

import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;

public final class SessionManager {

    private static Utilisateur utilisateurCourant = null;

    private SessionManager() {}

    public static void ouvrir(Utilisateur utilisateur) {
        utilisateurCourant = utilisateur;
    }

    public static Utilisateur getUtilisateur() {
        return utilisateurCourant;
    }

    public static boolean estConnecte() {
        return utilisateurCourant != null;
    }

    public static void fermer() {
        utilisateurCourant = null;
    }
}
