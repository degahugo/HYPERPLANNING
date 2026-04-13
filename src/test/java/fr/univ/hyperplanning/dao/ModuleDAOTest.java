package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.enums.NiveauGroupe;
import fr.univ.hyperplanning.model.pedagogie.ModuleCours;
import fr.univ.hyperplanning.model.pedagogie.SyllabusSemestre;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModuleDAO – findAll et findById")
class ModuleDAOTest {

    private static ModuleCours      testModule;
    private static SyllabusSemestre testSyllabus;

    private final ModuleDAO dao = new ModuleDAO();

    //Initialisation

    @BeforeAll
    static void creerModuleDeTest() {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();

            testSyllabus = new SyllabusSemestre();
            testSyllabus.setAnneeUniversitaire("TEST-2099-MOD");
            testSyllabus.setSemestre(2);
            testSyllabus.setNiveau(NiveauGroupe.L2);
            testSyllabus.setDateDebut(Date.valueOf(LocalDate.of(2099, 2, 1)));
            testSyllabus.setDateFin(Date.valueOf(LocalDate.of(2099, 6, 30)));
            em.persist(testSyllabus);

            testModule = new ModuleCours();
            testModule.setCode("TEST_MOD_DAO_FINDALL");
            testModule.setNom("Module Test FindAll");
            testModule.setNombreHeures(40);
            testModule.setCoefficient(2.0f);
            testModule.setSemestre(2);
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
    static void supprimerModuleDeTest() {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            // Le syllabus cascade → module (orphanRemoval = true)
            if (testSyllabus != null) {
                SyllabusSemestre s = em.find(SyllabusSemestre.class, testSyllabus.getId());
                if (s != null) em.remove(s);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    //Test

    @Test
    @DisplayName("findAll() : la liste contient le module de test créé")
    void findAll_contient_le_module_de_test() {
        List<ModuleCours> result = dao.findAll();

        assertNotNull(result, "findAll() ne doit pas retourner null");
        assertFalse(result.isEmpty(), "La liste ne doit pas être vide");
        assertTrue(
                result.stream().anyMatch(m -> m.getCode().equals("TEST_MOD_DAO_FINDALL")),
                "Le module de test doit être présent dans findAll()"
        );
    }

    @Test
    @DisplayName("findById() : retourne le bon module avec le bon code")
    void findById_retourne_le_bon_module() {
        ModuleCours result = dao.findById(testModule.getId());

        assertNotNull(result, "findById() doit retourner le module");
        assertEquals("TEST_MOD_DAO_FINDALL", result.getCode());
        assertEquals("Module Test FindAll",  result.getNom());
        assertEquals(40,                     result.getNombreHeures());
    }

    @Test
    @DisplayName("findById() avec un ID inexistant → retourne null")
    void findById_id_inexistant_retourne_null() {
        ModuleCours result = dao.findById(Long.MAX_VALUE);

        assertNull(result, "Un ID inexistant doit retourner null");
    }
}
