package fr.univ.hyperplanning.auth;

import fr.univ.hyperplanning.infra.config.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthService – hasher()")
class HasherTest {

    @Test
    @DisplayName("SHA-256 de la chaîne vide correspond à la valeur standard connue")
    void hasher_chaine_vide_sha256_standard() {
        // Valeur SHA-256("") universellement connue (RFC)
        String expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        assertEquals(expected, AuthService.hasher(""));
    }

    @Test
    @DisplayName("Le hash produit toujours 64 caractères hexadécimaux minuscules")
    void hasher_longueur_64_hex_minuscule() {
        String h = AuthService.hasher("monMotDePasse");
        assertNotNull(h);
        assertEquals(64, h.length(), "SHA-256 → 256 bits = 32 octets = 64 caractères hex");
        assertTrue(h.matches("[0-9a-f]+"), "Le hash doit être en hexadécimal minuscule");
    }

    @Test
    @DisplayName("Un même mot de passe produit toujours le même hash (déterminisme)")
    void hasher_meme_entree_meme_sortie() {
        assertEquals(
                AuthService.hasher("test1234"),
                AuthService.hasher("test1234"),
                "Le hashage doit être déterministe"
        );
    }

    @Test
    @DisplayName("Deux mots de passe différents produisent des hashs différents (la casse compte)")
    void hasher_entrees_differentes_sorties_differentes() {
        assertNotEquals(
                AuthService.hasher("motdepasse"),
                AuthService.hasher("MotDePasse"),
                "La casse est significative pour le hashage"
        );
    }

    @Test
    @DisplayName("Le hash ne contient jamais le mot de passe en clair")
    void hasher_ne_contient_pas_le_mdp_en_clair() {
        String password = "monSuperMotDePasse";
        assertFalse(
                AuthService.hasher(password).contains(password),
                "Le mot de passe en clair ne doit jamais apparaître dans son hash"
        );
    }
}
