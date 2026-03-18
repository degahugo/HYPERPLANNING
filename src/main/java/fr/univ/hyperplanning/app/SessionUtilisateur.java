package fr.univ.hyperplanning.app;

import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;

public class SessionUtilisateur {

    private static Utilisateur utilisateur;

    public static void set(Utilisateur u) {
        utilisateur = u;
    }

    public static Utilisateur get() {
        return utilisateur;
    }

    public static void clear() {
        utilisateur = null;
    }
}
