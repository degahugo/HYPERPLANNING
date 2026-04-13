package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.enums.NiveauGroupe;
import fr.univ.hyperplanning.model.enums.TypeCours;
import fr.univ.hyperplanning.model.enums.TypeSalle;
import fr.univ.hyperplanning.model.pedagogie.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SalleDAO – findSallesDisponibles()")
class SalleDisponibiliteTest {

    // Créneau de test bien dans le futur -> jamais en conflit avec les données seed
    private static final LocalDateTime DEBUT  = LocalDateTime.of(2099, 6, 15, 8,  0);
    private static final LocalDateTime FIN    = LocalDateTime.of(2099, 6, 15, 10, 0);

    private static Salle            testSalle;
    private static SyllabusSemestre testSyllabus;
    private static ModuleCours      testModule;

    private final SalleDAO dao = new SalleDAO();

    //Initialisation

    @BeforeAll
    static void creerDonneesDeReference() {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();

            testSalle = new Salle();
            testSalle.setNumero("TEST_SALLE_DISPO");
            testSalle.setBatiment("Bat DISPO");
            testSalle.setCapacite(30);
            testSalle.setTypeSalle(TypeSalle.SALLE_COURS);
            testSalle.setActif(true);
            em.persist(testSalle);

            testSyllabus = new SyllabusSemestre();
            testSyllabus.setAnneeUniversitaire("TEST-2099-DISPO");
            testSyllabus.setSemestre(1);
            testSyllabus.setNiveau(NiveauGroupe.L1);
            testSyllabus.setDateDebut(Date.valueOf(LocalDate.of(2099, 9, 1)));
            testSyllabus.setDateFin(Date.valueOf(LocalDate.of(2100, 1, 31)));
            em.persist(testSyllabus);

            testModule = new ModuleCours();
            testModule.setCode("TEST_MOD_DISPO");
            testModule.setNom("Module Dispo");
            testModule.setNombreHeures(20);
            testModule.setCoefficient(1.0f);
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

    @AfterAll
    static void supprimerDonneesDeReference() {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            //Cours de test
            List<Long> ids = em.createQuery(
                            "SELECT c.id FROM Cours c WHERE c.intitule LIKE 'TEST_DISPO_%'", Long.class)
                    .getResultList();
            for (Long id : ids) {
                em.createNativeQuery("DELETE FROM cours_groupe WHERE cours_id = ?").setParameter(1, id).executeUpdate();
                em.createNativeQuery("DELETE FROM cours WHERE id = ?").setParameter(1, id).executeUpdate();
            }
            //Syllabus (cascade → module)
            if (testSyllabus != null) {
                SyllabusSemestre s = em.find(SyllabusSemestre.class, testSyllabus.getId());
                if (s != null) em.remove(s);
            }
            //Salle
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
                            "SELECT c.id FROM Cours c WHERE c.intitule LIKE 'TEST_DISPO_%'", Long.class)
                    .getResultList();
            for (Long id : ids) {
                em.createNativeQuery("DELETE FROM cours_groupe WHERE cours_id = ?").setParameter(1, id).executeUpdate();
                em.createNativeQuery("DELETE FROM cours WHERE id = ?").setParameter(1, id).executeUpdate();
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    //Tests

    @Test
    @DisplayName("Salle sans cours : apparaît dans les disponibles pour n'importe quel créneau")
    void salle_libre_apparait_comme_disponible() {
        // Aucun cours créé : la salle est libre
        List<Salle> result = dao.findSallesDisponibles(DEBUT, FIN);

        assertTrue(result.stream().anyMatch(s -> s.getId().equals(testSalle.getId())),
                "Une salle sans cours doit être retournée comme disponible");
    }

    @Test
    @DisplayName("Salle occupée sur un créneau : absente des disponibles pour CE créneau")
    void salle_occupee_absente_des_disponibles() {
        // On crée un cours qui occupe exactement le créneau DEBUT -> FIN
        creerCours("TEST_DISPO_Cours Occupe", DEBUT, FIN);

        List<Salle> result = dao.findSallesDisponibles(DEBUT, FIN);

        assertFalse(result.stream().anyMatch(s -> s.getId().equals(testSalle.getId())),
                "Une salle occupée ne doit PAS apparaître dans les disponibles");
    }

    @Test
    @DisplayName("Salle occupée le matin : libre l'après-midi (créneaux disjoints)")
    void salle_occupee_matin_libre_apres_midi() {
        // Cours de 08h à 10h
        creerCours("TEST_DISPO_Cours Matin", DEBUT, FIN);

        // On cherche une dispo de 14h à 16h : la salle doit être libre
        LocalDateTime debut2 = LocalDateTime.of(2099, 6, 15, 14, 0);
        LocalDateTime fin2   = LocalDateTime.of(2099, 6, 15, 16, 0);
        List<Salle> result = dao.findSallesDisponibles(debut2, fin2);

        assertTrue(result.stream().anyMatch(s -> s.getId().equals(testSalle.getId())),
                "La salle occupée de 08h à 10h doit être libre de 14h à 16h");
    }

    // Creation de cours

    private void creerCours(String intitule, LocalDateTime debut, LocalDateTime fin) {
        // On utilise nouveauCours() (em.merge) qui gère les entités détachées
        Cours c = new Cours();
        c.setIntitule(intitule);
        c.setTypeCours(TypeCours.CM);
        c.setDateDebut(debut);
        c.setDateFin(fin);
        c.setDuree((int) java.time.Duration.between(debut, fin).toMinutes());
        c.setEstCC(false);
        c.setEstAnnule(false);
        c.setAnnotation("Test dispo");
        c.setModule(testModule);
        c.setSalle(testSalle);
        new CoursDAO().nouveauCours(c);
    }
}
