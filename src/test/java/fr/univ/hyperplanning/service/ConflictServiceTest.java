package fr.univ.hyperplanning.service;

import fr.univ.hyperplanning.model.pedagogie.Cours;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConflictService – logique de détection des chevauchements (isOverlapping)")
class ConflictServiceTest {

    // Une seule instance pour toute la classe (le constructeur crée une connexion DB)
    private static ConflictService service;

    @BeforeAll
    static void creerService() {
        service = new ConflictService();
    }

    @Test
    @DisplayName("Chevauchement partiel : C2 commence avant la fin de C1 → conflit détecté")
    void isOverlapping_chevauchement_partiel_detecte() {
        // C1: 08h-10h  |  C2: 09h-11h  : chevauchement de 09h à 10h
        Cours c1 = coursAvecDates(h(8, 0), h(10, 0));
        Cours c2 = coursAvecDates(h(9, 0), h(11, 0));

        assertTrue(service.isOverlapping(c1, c2),
                "Un chevauchement partiel doit être détecté");
    }

    @Test
    @DisplayName("Cours encapsulé : C2 entièrement dans C1 → conflit détecté")
    void isOverlapping_cours_encapsule_detecte() {
        // C1: 08h-12h  |  C2: 09h-11h  :  C2 est à l'intérieur de C1
        Cours c1 = coursAvecDates(h(8, 0),  h(12, 0));
        Cours c2 = coursAvecDates(h(9, 0),  h(11, 0));

        assertTrue(service.isOverlapping(c1, c2),
                "Un cours encapsulé dans un autre doit être détecté comme conflit");
    }

    @Test
    @DisplayName("Même plage horaire exacte → conflit détecté")
    void isOverlapping_meme_plage_exacte_detecte() {
        Cours c1 = coursAvecDates(h(10, 0), h(12, 0));
        Cours c2 = coursAvecDates(h(10, 0), h(12, 0));

        assertTrue(service.isOverlapping(c1, c2),
                "Deux cours aux horaires identiques sont en conflit");
    }

    @Test
    @DisplayName("Cours consécutifs (bout à bout) → PAS de conflit")
    void isOverlapping_cours_consecutifs_pas_de_conflit() {
        // C1: 08h-10h  |  C2: 10h-12h  :  C2 commence exactement quand C1 finit
        Cours c1 = coursAvecDates(h(8, 0),  h(10, 0));
        Cours c2 = coursAvecDates(h(10, 0), h(12, 0));

        assertFalse(service.isOverlapping(c1, c2),
                "Des cours consécutifs (bout à bout) ne sont PAS en conflit");
    }

    @Test
    @DisplayName("Cours disjoints (large écart) → PAS de conflit")
    void isOverlapping_cours_disjoints_pas_de_conflit() {
        // C1: 08h-10h  |  C2: 14h-16h  →  aucun chevauchement
        Cours c1 = coursAvecDates(h(8, 0),  h(10, 0));
        Cours c2 = coursAvecDates(h(14, 0), h(16, 0));

        assertFalse(service.isOverlapping(c1, c2),
                "Des cours disjoints ne doivent PAS être en conflit");
    }


    /** Crée un Cours avec uniquement les dates (suffisant pour isOverlapping) */
    private Cours coursAvecDates(LocalDateTime debut, LocalDateTime fin) {
        Cours c = new Cours();
        c.setDateDebut(debut);
        c.setDateFin(fin);
        return c;
    }

    /** Raccourci pour créer un LocalDateTime le 12/04/2026 à l'heure donnée */
    private LocalDateTime h(int heure, int minute) {
        return LocalDateTime.of(2026, 4, 12, heure, minute);
    }
}
