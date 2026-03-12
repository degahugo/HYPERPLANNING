package fr.univ.hyperplanning.app.controllers.Authentification;

import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;

public final class ReinitialisationContext {

    private static Utilisateur utilisateur;

    private ReinitialisationContext() {}

    public static void setUtilisateur(Utilisateur u) { utilisateur = u; }

    public static Utilisateur getUtilisateur() { return utilisateur; }

    public static void clear() { utilisateur = null; }
}
