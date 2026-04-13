package fr.univ.hyperplanning.auth;

import fr.univ.hyperplanning.dao.UtilisateurDAO;
import fr.univ.hyperplanning.infra.config.AuthService;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.infra.config.SessionManager;
import fr.univ.hyperplanning.model.utilisateurs.Etudiant;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthService – connecter() et deconnecter()")
class AuthServiceConnexionTest {

    private static final String EMAIL_TEST   = "auth.connexion.test@test.fr";
    private static final String MOT_DE_PASSE = "passConnexion1234";

    // Utilisateur créé une seule fois pour toute la classe
    private static Utilisateur utilisateurCree;

    @BeforeAll
    static void creerUtilisateurDeTest() {
        // Nettoyage préventif (crash d'un test précédent)
        supprimerParEmail(EMAIL_TEST);
        utilisateurCree = AuthService.inscrire("Connexion", "Test", EMAIL_TEST, MOT_DE_PASSE);
    }

    @AfterAll
    static void supprimerUtilisateurDeTest() {
        supprimerParEmail(EMAIL_TEST);
    }

    // On s'assure que la session est fermée avant chaque test
    @BeforeEach
    void fermerSessionAvantTest() {
        SessionManager.fermer();
    }

    @Test
    @DisplayName("Bons identifiants → l'utilisateur est retourné")
    void connecter_bons_identifiants_retourne_utilisateur() {
        String username = UtilisateurDAO.buildUsername(utilisateurCree);

        Utilisateur result = AuthService.connecter(username, MOT_DE_PASSE);

        assertNotNull(result, "La connexion réussie doit retourner l'utilisateur");
        assertEquals(EMAIL_TEST, result.getEmail());
    }

    @Test
    @DisplayName("Bons identifiants → la session est ouverte")
    void connecter_bons_identifiants_ouvre_la_session() {
        String username = UtilisateurDAO.buildUsername(utilisateurCree);

        AuthService.connecter(username, MOT_DE_PASSE);

        assertTrue(SessionManager.estConnecte(),
                "La connexion réussie doit ouvrir la session");
        assertEquals(EMAIL_TEST, SessionManager.getUtilisateur().getEmail());
    }

    @Test
    @DisplayName("Mauvais mot de passe → null est retourné et la session reste fermée")
    void connecter_mauvais_mot_de_passe_retourne_null() {
        String username = UtilisateurDAO.buildUsername(utilisateurCree);

        Utilisateur result = AuthService.connecter(username, "mauvaisMotDePasse!");

        assertNull(result, "Un mauvais mot de passe doit retourner null");
        assertFalse(SessionManager.estConnecte(), "La session ne doit pas s'ouvrir");
    }

    @Test
    @DisplayName("Username inexistant → null est retourné et la session reste fermée")
    void connecter_username_inconnu_retourne_null() {
        Utilisateur result = AuthService.connecter("UsernameQuiNExistePas999", MOT_DE_PASSE);

        assertNull(result, "Un username inconnu doit retourner null");
        assertFalse(SessionManager.estConnecte());
    }

    @Test
    @DisplayName("deconnecter() ferme la session active")
    void deconnecter_ferme_la_session() {
        String username = UtilisateurDAO.buildUsername(utilisateurCree);
        AuthService.connecter(username, MOT_DE_PASSE);
        assertTrue(SessionManager.estConnecte()); // pré-condition

        AuthService.deconnecter();

        assertFalse(SessionManager.estConnecte(), "La déconnexion doit fermer la session");
        assertNull(SessionManager.getUtilisateur());
    }

    // ─── Utilitaire (static car utilisé dans @BeforeAll / @AfterAll) ──────────
    private static void supprimerParEmail(String email) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            try {
                Etudiant etu = em.createQuery(
                                "SELECT e FROM Etudiant e WHERE e.email = :e", Etudiant.class)
                        .setParameter("e", email)
                        .getSingleResult();
                em.remove(etu);
            } catch (NoResultException ignored) {}
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
