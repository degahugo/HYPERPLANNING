package fr.univ.hyperplanning.auth;

import fr.univ.hyperplanning.infra.config.AuthService;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.enums.RoleUtilisateur;
import fr.univ.hyperplanning.model.utilisateurs.Etudiant;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthService – inscrire()")
class AuthServiceInscriptionTest {

    // Email propre à cette classe de test pour éviter tout conflit avec les données seed
    private static final String EMAIL_TEST = "auth.inscription.test@test.fr";

    // Nettoyage AVANT (si le test précédent a crashé) ET APRÈS chaque test
    @BeforeEach
    @AfterEach
    void nettoyerUtilisateurDeTest() {
        supprimerParEmail(EMAIL_TEST);
    }

    @Test
    @DisplayName("Données valides → crée un Etudiant actif avec le bon rôle")
    void inscrire_donnees_valides_cree_etudiant_actif() {
        Utilisateur result = AuthService.inscrire("Test", "Inscription", EMAIL_TEST, "motdepasse");

        assertNotNull(result);
        assertInstanceOf(Etudiant.class, result, "L'inscription doit créer un Etudiant");
        assertEquals("TEST", result.getNom(),    "Le nom doit être stocké en majuscules");
        assertEquals("Inscription", result.getPrenom());
        assertEquals(EMAIL_TEST, result.getEmail());
        assertEquals(RoleUtilisateur.ETUDIANT, result.getRole());
        assertTrue(result.isActif(), "L'utilisateur créé doit être actif");
        assertNotNull(result.getId(), "L'ID doit être assigné par la base");
    }

    @Test
    @DisplayName("Le mot de passe n'est jamais stocké en clair – le hash SHA-256 est correct")
    void inscrire_mot_de_passe_est_hache_et_non_en_clair() {
        String motDePasse = "monMotDePasseSecret";

        Utilisateur result = AuthService.inscrire("Test", "Inscription", EMAIL_TEST, motDePasse);

        assertNotEquals(motDePasse, result.getMotDePasseHash(),
                "Le mot de passe en clair ne doit JAMAIS être stocké directement");
        assertEquals(AuthService.hasher(motDePasse), result.getMotDePasseHash(),
                "Le hash stocké doit correspondre au SHA-256 du mot de passe");
    }

    @Test
    @DisplayName("Email déjà utilisé → IllegalArgumentException avec message explicite")
    void inscrire_email_deja_utilise_leve_exception() {
        // Première inscription (OK)
        AuthService.inscrire("Test", "Inscription", EMAIL_TEST, "pass1234");

        // Deuxième inscription avec le même email (doit échouer)
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> AuthService.inscrire("Autre", "Personne", EMAIL_TEST, "pass5678"),
                "Un doublon d'email doit lever une IllegalArgumentException"
        );
        assertNotNull(ex.getMessage(), "Le message d'erreur ne doit pas être null");
    }

    @Test
    @DisplayName("Email invalide (sans '@' et domaine) → IllegalArgumentException")
    void inscrire_email_invalide_leve_exception() {
        assertThrows(
                IllegalArgumentException.class,
                () -> AuthService.inscrire("Test", "Inscription", "email-pas-valide", "pass1234"),
                "Un email sans format valide doit lever une exception"
        );
    }

    // ─── Utilitaire ───────────────────────────────────────────────────────────
    private void supprimerParEmail(String email) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            try {
                Etudiant etu = em.createQuery(
                                "SELECT e FROM Etudiant e WHERE e.email = :e", Etudiant.class)
                        .setParameter("e", email)
                        .getSingleResult();
                em.remove(etu);
            } catch (NoResultException ignored) { /* déjà absent, pas de problème */ }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
