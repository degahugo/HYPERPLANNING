package fr.univ.hyperplanning.auth;

import fr.univ.hyperplanning.infra.config.SessionManager;
import fr.univ.hyperplanning.model.utilisateurs.Etudiant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SessionManager – gestion de la session utilisateur")
class SessionManagerTest {

    // On repart d'une session vide avant chaque test
    @BeforeEach
    void reinitialiserSession() {
        SessionManager.fermer();
    }

    @Test
    @DisplayName("Sans connexion : getUtilisateur() retourne null")
    void session_fermee_getUtilisateur_retourne_null() {
        assertNull(SessionManager.getUtilisateur());
    }

    @Test
    @DisplayName("Sans connexion : estConnecte() retourne false")
    void session_fermee_estConnecte_retourne_false() {
        assertFalse(SessionManager.estConnecte());
    }

    @Test
    @DisplayName("Après ouvrir() : l'utilisateur est accessible et estConnecte() retourne true")
    void ouvrir_session_rend_utilisateur_courant() {
        Etudiant etu = creerEtudiantFactice("Durand", "Alice");

        SessionManager.ouvrir(etu);

        assertEquals(etu, SessionManager.getUtilisateur(),
                "getUtilisateur() doit retourner exactement l'objet passé à ouvrir()");
        assertTrue(SessionManager.estConnecte());
    }

    @Test
    @DisplayName("Après fermer() : l'utilisateur est null et la session est inactive")
    void fermer_session_efface_utilisateur() {
        SessionManager.ouvrir(creerEtudiantFactice("Durand", "Alice"));
        assertTrue(SessionManager.estConnecte()); // pré-condition

        SessionManager.fermer();

        assertNull(SessionManager.getUtilisateur());
        assertFalse(SessionManager.estConnecte());
    }

    @Test
    @DisplayName("Ouvrir une 2ème session remplace la précédente")
    void ouvrir_deux_fois_remplace_la_session() {
        Etudiant etu1 = creerEtudiantFactice("Dupont", "Jean");
        Etudiant etu2 = creerEtudiantFactice("Martin", "Sara");

        SessionManager.ouvrir(etu1);
        SessionManager.ouvrir(etu2);

        assertEquals(etu2, SessionManager.getUtilisateur(),
                "La deuxième ouverture doit écraser la première");
    }

    // ─── Utilitaire ───────────────────────────────────────────────────────────
    private Etudiant creerEtudiantFactice(String nom, String prenom) {
        Etudiant etu = new Etudiant();
        etu.setNom(nom);
        etu.setPrenom(prenom);
        return etu;
    }
}
