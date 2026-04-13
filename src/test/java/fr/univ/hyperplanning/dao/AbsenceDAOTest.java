package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.infra.config.AuthService;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.enums.NiveauGroupe;
import fr.univ.hyperplanning.model.enums.StatutAbsence;
import fr.univ.hyperplanning.model.enums.TypeCours;
import fr.univ.hyperplanning.model.enums.TypeSalle;
import fr.univ.hyperplanning.model.gestion.Absence;
import fr.univ.hyperplanning.model.pedagogie.*;
import fr.univ.hyperplanning.model.utilisateurs.Etudiant;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AbsenceDAO – sauvegarder, find et count")
class AbsenceDAOTest {

    private static final String EMAIL_TEST  = "absence.dao.test@test.fr";
    private static final String MOT_PASSE   = "pass1234";

    private static Utilisateur  testEtudiant;
    private static Cours        testCours;
    private static SyllabusSemestre testSyllabus;
    private static Salle        testSalle;

    private final AbsenceDAO dao = new AbsenceDAO();

    // Inititalisation

    @BeforeAll
    static void creerDonneesDeReference() {
        // 1. Créer l'étudiant
        supprimerEtudiantParEmail(EMAIL_TEST); // guard si crash précédent
        testEtudiant = AuthService.inscrire("Absence", "Test", EMAIL_TEST, MOT_PASSE);


        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();

            testSalle = new Salle();
            testSalle.setNumero("TEST_SALLE_ABS");
            testSalle.setBatiment("Bat ABS");
            testSalle.setCapacite(30);
            testSalle.setTypeSalle(TypeSalle.SALLE_COURS);
            testSalle.setActif(true);
            em.persist(testSalle);

            testSyllabus = new SyllabusSemestre();
            testSyllabus.setAnneeUniversitaire("TEST-2099-ABS");
            testSyllabus.setSemestre(1);
            testSyllabus.setNiveau(NiveauGroupe.L1);
            testSyllabus.setDateDebut(Date.valueOf(LocalDate.of(2099, 9, 1)));
            testSyllabus.setDateFin(Date.valueOf(LocalDate.of(2100, 1, 31)));
            em.persist(testSyllabus);

            ModuleCours module = new ModuleCours();
            module.setCode("TEST_MOD_ABS");
            module.setNom("Module Absence Test");
            module.setNombreHeures(20);
            module.setCoefficient(1.0f);
            module.setSemestre(1);
            module.setSyllabus(testSyllabus);
            em.persist(module);

            testCours = new Cours();
            testCours.setIntitule("TEST_ABS_Cours");
            testCours.setTypeCours(TypeCours.CM);
            testCours.setDateDebut(LocalDateTime.of(2099, 6, 15, 8, 0));
            testCours.setDateFin(LocalDateTime.of(2099, 6, 15, 10, 0));
            testCours.setDuree(120);
            testCours.setEstCC(false);
            testCours.setEstAnnule(false);
            testCours.setAnnotation("Test absence");
            testCours.setModule(module);
            testCours.setSalle(testSalle);
            em.persist(testCours);

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

            // 1. Absences restantes
            em.createNativeQuery("DELETE FROM absence_pieces_jointes WHERE absence_id IN " +
                            "(SELECT id FROM absences WHERE cours_id = ?)")
                    .setParameter(1, testCours.getId()).executeUpdate();
            em.createNativeQuery("DELETE FROM absences WHERE cours_id = ?")
                    .setParameter(1, testCours.getId()).executeUpdate();

            // 2. Cours
            em.createNativeQuery("DELETE FROM cours WHERE id = ?")
                    .setParameter(1, testCours.getId()).executeUpdate();

            // 3. Syllabus (cascade → module)
            SyllabusSemestre s = em.find(SyllabusSemestre.class, testSyllabus.getId());
            if (s != null) em.remove(s);

            // 4. Salle
            Salle sl = em.find(Salle.class, testSalle.getId());
            if (sl != null) em.remove(sl);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
        }
        // 5. Étudiant
        supprimerEtudiantParEmail(EMAIL_TEST);
    }

    // Nettoyage des absences de test entre chaque test
    @BeforeEach
    @AfterEach
    void nettoyerAbsencesDeTest() {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            em.createNativeQuery("DELETE FROM absence_pieces_jointes WHERE absence_id IN " +
                            "(SELECT id FROM absences WHERE cours_id = ?)")
                    .setParameter(1, testCours.getId()).executeUpdate();
            em.createNativeQuery("DELETE FROM absences WHERE cours_id = ?")
                    .setParameter(1, testCours.getId()).executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    // ─── Tests ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("sauvegarder() → l'absence est persistée et retrouvable par utilisateur")
    void sauvegarder_persiste_absence_retrouvable_en_base() {
        Absence abs = buildAbsence(StatutAbsence.DECLAREE);

        dao.sauvegarder(abs);

        long count = dao.countAbsencesByUtilisateur(testEtudiant.getId());
        assertEquals(1, count, "L'absence doit être retrouvable après sauvegarder()");
    }

    @Test
    @DisplayName("findAbsencesAValider() → inclut les absences au statut DECLAREE")
    void findAbsencesAValider_inclut_absences_declarees() {
        persisterAbsence(StatutAbsence.DECLAREE);

        List<Absence> result = dao.findAbsencesAValider();

        assertTrue(
                result.stream().anyMatch(a ->
                        a.getUtilisateurAbsent().getId().equals(testEtudiant.getId())),
                "Une absence DECLAREE doit apparaître dans la liste à valider"
        );
    }

    @Test
    @DisplayName("findAbsencesAValider() → exclut les absences déjà VALIDEE")
    void findAbsencesAValider_exclut_absences_validees() {
        persisterAbsence(StatutAbsence.VALIDEE);

        List<Absence> result = dao.findAbsencesAValider();

        assertFalse(
                result.stream().anyMatch(a ->
                        a.getUtilisateurAbsent().getId().equals(testEtudiant.getId())),
                "Une absence VALIDEE ne doit PAS apparaître dans la liste à valider"
        );
    }

    @Test
    @DisplayName("findAbsencesByUtilisateur() → retourne uniquement les absences de cet utilisateur")
    void findAbsencesByUtilisateur_retourne_absences_du_bon_utilisateur() {
        persisterAbsence(StatutAbsence.DECLAREE);

        List<Absence> result = dao.findAbsencesByUtilisateur(testEtudiant.getId());

        assertFalse(result.isEmpty(), "La liste ne doit pas être vide");
        assertTrue(
                result.stream().allMatch(a ->
                        a.getUtilisateurAbsent().getId().equals(testEtudiant.getId())),
                "Toutes les absences retournées doivent appartenir au bon étudiant"
        );
    }

    @Test
    @DisplayName("countAbsencesAValider() → le compteur augmente d'1 après un ajout DECLAREE")
    void countAbsencesAValider_increment_apres_ajout() {
        long avant = dao.countAbsencesAValider();

        persisterAbsence(StatutAbsence.DECLAREE);

        long apres = dao.countAbsencesAValider();
        assertEquals(avant + 1, apres,
                "Le compteur doit augmenter de 1 après ajout d'une absence DECLAREE");
    }

    @Test
    @DisplayName("countAbsencesByUtilisateur() → compte exactement les absences de cet utilisateur")
    void countAbsencesByUtilisateur_compte_correctement() {
        assertEquals(0, dao.countAbsencesByUtilisateur(testEtudiant.getId()),
                "Aucune absence au départ");

        persisterAbsence(StatutAbsence.DECLAREE);
        persisterAbsence(StatutAbsence.VALIDEE);

        assertEquals(2, dao.countAbsencesByUtilisateur(testEtudiant.getId()),
                "2 absences créées doivent être comptées");
    }

    // ─── Utilitaires ──────────────────────────────────────────────────────────

    /**
     * Construit une Absence avec des entités re-fetchées dans un EM frais.
     * Utiliser pour tester dao.sauvegarder() directement.
     * ⚠ Si "detached entity" : changer em.persist → em.merge dans AbsenceDAO.sauvegarder()
     */
    private Absence buildAbsence(StatutAbsence statut) {
        EntityManager em = JpaUtil.em();
        Utilisateur etudiant = em.find(Utilisateur.class, testEtudiant.getId());
        Cours cours          = em.find(Cours.class, testCours.getId());
        em.close();

        Absence a = new Absence();
        a.setDateAbsence(Date.valueOf(LocalDate.of(2099, 6, 15)));
        a.setMotif("TEST_ABS Maladie");
        a.setStatut(statut);
        a.setDateDeclaration(LocalDateTime.now());
        a.setUtilisateurAbsent(etudiant);
        a.setDeclareePar(etudiant);
        a.setCours(cours);
        return a;
    }

    /**
     * Persiste directement une absence via JpaUtil (bypass AbsenceDAO).
     * Utiliser pour préparer l'état avant de tester les méthodes de requête.
     */
    private void persisterAbsence(StatutAbsence statut) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            Utilisateur e = em.find(Utilisateur.class, testEtudiant.getId());
            Cours c       = em.find(Cours.class, testCours.getId());

            Absence a = new Absence();
            a.setDateAbsence(Date.valueOf(LocalDate.of(2099, 6, 15)));
            a.setMotif("TEST_ABS_" + statut.name());
            a.setStatut(statut);
            a.setDateDeclaration(LocalDateTime.now());
            a.setUtilisateurAbsent(e);
            a.setDeclareePar(e);
            a.setCours(c);
            em.persist(a);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    private static void supprimerEtudiantParEmail(String email) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            try {
                Etudiant etu = em.createQuery(
                                "SELECT e FROM Etudiant e WHERE e.email = :e", Etudiant.class)
                        .setParameter("e", email).getSingleResult();
                em.remove(etu);
            } catch (jakarta.persistence.NoResultException ignored) {}
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
