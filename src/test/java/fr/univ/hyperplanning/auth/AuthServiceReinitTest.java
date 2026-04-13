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

@DisplayName("AuthService – verifierIdentiteReinitialisation() et reinitialiserMotDePasse()")
class AuthServiceReinitTest {

    private static final String EMAIL_TEST   = "auth.reinit.test@test.fr";
    private static final String MOT_DE_PASSE = "ancienMotDePasse99";

    private static Utilisateur utilisateurCree;

    @BeforeAll
    static void creerUtilisateurDeTest() {
        supprimerParEmail(EMAIL_TEST);
        utilisateurCree = AuthService.inscrire("Reinit", "Test", EMAIL_TEST, MOT_DE_PASSE);
    }

    @AfterAll
    static void supprimerUtilisateurDeTest() {
        supprimerParEmail(EMAIL_TEST);
    }

    @BeforeEach
    void fermerSession() {
        SessionManager.fermer();
    }

    @Test
    @DisplayName("Email + username corrects → l'utilisateur est retourné")
    void verifierIdentite_email_et_username_corrects_retourne_utilisateur() {
        String username = UtilisateurDAO.buildUsername(utilisateurCree);

        Utilisateur result = AuthService.verifierIdentiteReinitialisation(EMAIL_TEST, username);

        assertNotNull(result, "Les bonnes informations doivent retourner l'utilisateur");
        assertEquals(EMAIL_TEST, result.getEmail());
    }

    @Test
    @DisplayName("Email inconnu → null est retourné")
    void verifierIdentite_email_inconnu_retourne_null() {
        String username = UtilisateurDAO.buildUsername(utilisateurCree);

        Utilisateur result = AuthService.verifierIdentiteReinitialisation(
                "email.inconnu.xyz@test.fr", username
        );

        assertNull(result, "Un email inconnu doit retourner null");
    }

    @Test
    @DisplayName("Username incorrect → null est retourné même si l'email est bon")
    void verifierIdentite_username_incorrect_retourne_null() {
        Utilisateur result = AuthService.verifierIdentiteReinitialisation(
                EMAIL_TEST, "mauvaisUsername99999"
        );

        assertNull(result, "Un username incorrect doit retourner null");
    }

    @Test
    @DisplayName("Après reinitialiserMotDePasse(), le nouveau hash est bien enregistré en base")
    void reinitialiserMotDePasse_nouveau_hash_persiste_en_base() {
        String nouveauMotDePasse = "nouveauMotDePasse999";

        AuthService.reinitialiserMotDePasse(utilisateurCree, nouveauMotDePasse);

        // On recharge depuis la DB pour ne pas tester l'objet en mémoire
        Utilisateur fromDb = new UtilisateurDAO().findByEmail(EMAIL_TEST);
        assertEquals(
                AuthService.hasher(nouveauMotDePasse),
                fromDb.getMotDePasseHash(),
                "Le nouveau hash doit être persisté en base"
        );
    }

    // ─── Utilitaire ───────────────────────────────────────────────────────────
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
