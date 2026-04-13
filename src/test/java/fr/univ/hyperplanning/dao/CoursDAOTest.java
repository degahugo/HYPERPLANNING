package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.enums.NiveauGroupe;
import fr.univ.hyperplanning.model.enums.TypeCours;
import fr.univ.hyperplanning.model.enums.TypeSalle;
import fr.univ.hyperplanning.model.pedagogie.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CoursDAO – requêtes de planification")
class CoursDAOTest {

    // Semaine de test : semaine du lundi 2099-06-08
    // Choisie loin dans le futur pour ne jamais interférer avec les données seed
    private static final LocalDate SEMAINE_TEST = LocalDate.of(2099, 6, 10)
            .with(DayOfWeek.MONDAY);

    private static Salle        testSalle;
    private static SyllabusSemestre testSyllabus;
    private static ModuleCours  testModule;

    private final CoursDAO dao = new CoursDAO();

    // Initialisation : créer les données de référence UNE SEULE FOIS

    @BeforeAll
    static void creerDonneesDeReference() {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();

            // 1. Salle de test
            testSalle = new Salle();
            testSalle.setNumero("TEST_SALLE_DAO");
            testSalle.setBatiment("Bat TEST");
            testSalle.setCapacite(30);
            testSalle.setTypeSalle(TypeSalle.SALLE_COURS);
            testSalle.setActif(true);
            em.persist(testSalle);

            // 2. SyllabusSemestre de test
            testSyllabus = new SyllabusSemestre();
            testSyllabus.setAnneeUniversitaire("TEST-2099-COURS");
            testSyllabus.setSemestre(1);
            testSyllabus.setNiveau(NiveauGroupe.L1);
            testSyllabus.setDateDebut(Date.valueOf(LocalDate.of(2099, 9, 1)));
            testSyllabus.setDateFin(Date.valueOf(LocalDate.of(2100, 1, 31)));
            em.persist(testSyllabus);

            // 3. ModuleCours de test
            testModule = new ModuleCours();
            testModule.setCode("TEST_MOD_DAO");
            testModule.setNom("Module Test DAO");
            testModule.setNombreHeures(30);
            testModule.setCoefficient(1.5f);
            testModule.setSemestre(1);
            testModule.setSyllabus(testSyllabus);
            em.persist(testModule);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    //Supression des données de références

    @AfterAll
    static void supprimerDonneesDeReference() {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();

            // Supprimer les cours de test (native SQL pour respecter les FK)
            List<Long> ids = em.createQuery(
                            "SELECT c.id FROM Cours c WHERE c.intitule LIKE 'TEST_%'", Long.class)
                    .getResultList();
            for (Long id : ids) {
                em.createNativeQuery("DELETE FROM cours_groupe   WHERE cours_id = ?").setParameter(1, id).executeUpdate();
                em.createNativeQuery("DELETE FROM enseignant_cours WHERE cours_id = ?").setParameter(1, id).executeUpdate();
                em.createNativeQuery("DELETE FROM intervenant_cours WHERE cours_id = ?").setParameter(1, id).executeUpdate();
                em.createNativeQuery("DELETE FROM cours WHERE id = ?").setParameter(1, id).executeUpdate();
            }

            // Supprimer le syllabus (cascade → supprime aussi le module)
            if (testSyllabus != null) {
                SyllabusSemestre s = em.find(SyllabusSemestre.class, testSyllabus.getId());
                if (s != null) em.remove(s);
            }

            // Supprimer la salle
            if (testSalle != null) {
                Salle s = em.find(Salle.class, testSalle.getId());
                if (s != null) em.remove(s);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    // Nettoyage des cours de test entre chaque test
    @BeforeEach
    @AfterEach
    void nettoyerCoursDeTest() {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            List<Long> ids = em.createQuery(
                            "SELECT c.id FROM Cours c WHERE c.intitule LIKE 'TEST_%'", Long.class)
                    .getResultList();
            for (Long id : ids) {
                em.createNativeQuery("DELETE FROM cours_groupe    WHERE cours_id = ?").setParameter(1, id).executeUpdate();
                em.createNativeQuery("DELETE FROM enseignant_cours WHERE cours_id = ?").setParameter(1, id).executeUpdate();
                em.createNativeQuery("DELETE FROM cours WHERE id = ?").setParameter(1, id).executeUpdate();
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    //TEST

    @Test
    @DisplayName("nouveauCours()  le cours est persisté et retrouvable dans findAll()")
    void nouveauCours_persiste_et_retrouvable() {
        Cours cours = buildCours("TEST_Algorithmique CM",
                TypeCours.CM,
                SEMAINE_TEST.atTime(8, 0),
                SEMAINE_TEST.atTime(10, 0));

        dao.nouveauCours(cours);

        List<Cours> tous = dao.findAll();
        assertTrue(tous.stream().anyMatch(c -> c.getIntitule().equals("TEST_Algorithmique CM")),
                "Le cours créé doit être présent dans findAll()");
    }

    @Test
    @DisplayName("findByDateRange()  retourne les cours dans la plage de dates")
    void findByDateRange_retourne_cours_dans_la_periode() {
        // Cours dans la plage
        dao.nouveauCours(buildCours("TEST_Maths CM",
                TypeCours.CM, SEMAINE_TEST.atTime(8, 0), SEMAINE_TEST.atTime(10, 0)));
        dao.nouveauCours(buildCours("TEST_Physique TD",
                TypeCours.TD, SEMAINE_TEST.plusDays(1).atTime(14, 0), SEMAINE_TEST.plusDays(1).atTime(16, 0)));

        List<Cours> result = dao.findByDateRange(SEMAINE_TEST, SEMAINE_TEST.plusDays(4));

        assertEquals(2, result.stream()
                .filter(c -> c.getIntitule().startsWith("TEST_"))
                .count(), "Les 2 cours de test doivent être dans la plage");
    }

    @Test
    @DisplayName("findByDateRange()  exclut les cours hors de la plage")
    void findByDateRange_exclut_cours_hors_periode() {
        // Cours dans la plage (cette semaine)
        dao.nouveauCours(buildCours("TEST_Info CM",
                TypeCours.CM, SEMAINE_TEST.atTime(9, 0), SEMAINE_TEST.atTime(11, 0)));
        // Cours HORS plage (2 semaines plus tard)
        dao.nouveauCours(buildCours("TEST_Info TD hors plage",
                TypeCours.TD,
                SEMAINE_TEST.plusWeeks(2).atTime(9, 0),
                SEMAINE_TEST.plusWeeks(2).atTime(11, 0)));

        List<Cours> result = dao.findByDateRange(SEMAINE_TEST, SEMAINE_TEST.plusDays(6));

        assertTrue(result.stream().anyMatch(c -> c.getIntitule().equals("TEST_Info CM")),
                "Le cours de la semaine doit être inclus");
        assertFalse(result.stream().anyMatch(c -> c.getIntitule().equals("TEST_Info TD hors plage")),
                "Le cours hors plage ne doit PAS être inclus");
    }

    @Test
    @DisplayName("findBySalle()  retourne uniquement les cours de la salle demandée")
    void findBySalle_filtre_par_salle() {
        dao.nouveauCours(buildCours("TEST_Cours en TEST_SALLE_DAO",
                TypeCours.CM, SEMAINE_TEST.atTime(8, 0), SEMAINE_TEST.atTime(10, 0)));

        List<Cours> result = dao.findBySalle(
                testSalle.getId(), SEMAINE_TEST, SEMAINE_TEST.plusDays(6));

        assertFalse(result.isEmpty(), "La requête doit retourner au moins un cours");
        assertTrue(result.stream().allMatch(c -> c.getSalle().getId().equals(testSalle.getId())),
                "Tous les cours retournés doivent être dans la salle filtrée");
    }

    @Test
    @DisplayName("findByWeek() → retourne les cours de toute la semaine du lundi au dimanche")
    void findByWeek_retourne_cours_de_la_semaine() {
        // Cours au milieu de la semaine (mercredi)
        dao.nouveauCours(buildCours("TEST_Cours Mercredi",
                TypeCours.TD,
                SEMAINE_TEST.plusDays(2).atTime(10, 0),  // mercredi
                SEMAINE_TEST.plusDays(2).atTime(12, 0)));

        // Cours la semaine suivante (ne doit pas remonter)
        dao.nouveauCours(buildCours("TEST_Cours semaine suivante",
                TypeCours.TD,
                SEMAINE_TEST.plusWeeks(1).atTime(10, 0),
                SEMAINE_TEST.plusWeeks(1).atTime(12, 0)));

        List<Cours> result = dao.findByWeek(SEMAINE_TEST);

        assertTrue(result.stream().anyMatch(c -> c.getIntitule().equals("TEST_Cours Mercredi")),
                "Le cours du mercredi doit être dans la semaine");
        assertFalse(result.stream().anyMatch(c -> c.getIntitule().equals("TEST_Cours semaine suivante")),
                "Le cours de la semaine suivante ne doit PAS remonter");
    }

    @Test
    @DisplayName("supprimerCours()  le cours n'est plus dans findAll() après suppression")
    void supprimerCours_cours_absent_apres_suppression() {
        Cours cours = buildCours("TEST_A Supprimer", TypeCours.CM,
                SEMAINE_TEST.atTime(8, 0), SEMAINE_TEST.atTime(10, 0));
        dao.nouveauCours(cours);

        // Retrouver le cours en DB pour avoir son ID
        Cours fromDb = dao.findAll().stream()
                .filter(c -> c.getIntitule().equals("TEST_A Supprimer"))
                .findFirst()
                .orElseThrow();

        dao.supprimerCours(fromDb);

        boolean encorePresent = dao.findAll().stream()
                .anyMatch(c -> c.getIntitule().equals("TEST_A Supprimer"));
        assertFalse(encorePresent,
                "Le cours doit être absent de findAll() après suppression");
    }

    // Creation des cours

    private Cours buildCours(String intitule, TypeCours type,
                             LocalDateTime debut, LocalDateTime fin) {
        Cours c = new Cours();
        c.setIntitule(intitule);
        c.setTypeCours(type);
        c.setDateDebut(debut);
        c.setDateFin(fin);
        c.setDuree((int) java.time.Duration.between(debut, fin).toMinutes());
        c.setEstCC(false);
        c.setEstAnnule(false);
        c.setAnnotation("Cours de test");
        c.setModule(testModule);
        c.setSalle(testSalle);
        return c;
    }
}
