package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.enums.NiveauGroupe;
import fr.univ.hyperplanning.model.enums.TypeGroupe;
import fr.univ.hyperplanning.model.pedagogie.Groupe;
import fr.univ.hyperplanning.model.pedagogie.SyllabusSemestre;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GroupeDAO – CRUD complet")
class GroupeDAOTest {

    private static final String CODE_PREFIX  = "TEST_GRP_";
    private static final String ANNEE_TEST   = "TEST-2099";

    private static SyllabusSemestre testSyllabus;

    private final GroupeDAO dao = new GroupeDAO();

    // Création d'un SyllabusSemestre de test (une seule fois pour toute la classe)
    @BeforeAll
    static void creerSyllabusDeTest() {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            // Guard : évite le doublon si le syllabus existe déjà
            try {
                testSyllabus = em.createQuery(
                                "SELECT s FROM SyllabusSemestre s WHERE s.anneeUniversitaire = :a AND s.semestre = 1 AND s.niveau = :n",
                                SyllabusSemestre.class)
                        .setParameter("a", ANNEE_TEST)
                        .setParameter("n", NiveauGroupe.L1)
                        .getSingleResult();
            } catch (NoResultException e) {
                testSyllabus = new SyllabusSemestre();
                testSyllabus.setAnneeUniversitaire(ANNEE_TEST);
                testSyllabus.setSemestre(1);
                testSyllabus.setNiveau(NiveauGroupe.L1);
                testSyllabus.setDateDebut(Date.valueOf(LocalDate.of(2099, 9, 1)));
                testSyllabus.setDateFin(Date.valueOf(LocalDate.of(2100, 1, 31)));
                em.persist(testSyllabus);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    // Suppression du syllabus de test à la fin de la classe
    @AfterAll
    static void supprimerSyllabusDeTest() {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            if (testSyllabus != null) {
                SyllabusSemestre s = em.find(SyllabusSemestre.class, testSyllabus.getId());
                if (s != null) em.remove(s); // cascade supprime aussi les ModuleCours
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    // Nettoyage des groupes de test avant et après chaque test
    @BeforeEach
    @AfterEach
    void nettoyerGroupesDeTest() {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            List<Groupe> aSupprimer = em.createQuery(
                            "SELECT g FROM Groupe g WHERE g.code LIKE :prefix", Groupe.class)
                    .setParameter("prefix", CODE_PREFIX + "%")
                    .getResultList();
            for (Groupe g : aSupprimer) {
                em.remove(em.contains(g) ? g : em.merge(g));
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    //CREATE

    @Test
    @DisplayName("save()  le groupe est persisté et retrouvable par son ID")
    void save_persiste_groupe_retrouvable_par_id() {
        Groupe groupe = buildGroupe(CODE_PREFIX + "001", "L1 TD1", NiveauGroupe.L1, TypeGroupe.TD);

        dao.save(groupe);

        // Récupérer l'ID depuis la DB
        Long id = trouverIdParCode(CODE_PREFIX + "001");
        assertNotNull(id, "Le groupe doit être retrouvable après save()");

        Groupe fromDb = dao.findById(id);
        assertNotNull(fromDb);
        assertEquals(CODE_PREFIX + "001",    fromDb.getCode());
        assertEquals("L1 TD1",               fromDb.getLibelle());
        assertEquals(NiveauGroupe.L1,         fromDb.getNiveau());
        assertEquals(TypeGroupe.TD,           fromDb.getTypeGroupe());
    }

    //READ

    @Test
    @DisplayName("findAll()  la liste contient les groupes de test créés")
    void findAll_contient_les_groupes_de_test() {
        dao.save(buildGroupe(CODE_PREFIX + "002", "L1 CM", NiveauGroupe.L1, TypeGroupe.CM));
        dao.save(buildGroupe(CODE_PREFIX + "003", "L2 TD", NiveauGroupe.L2, TypeGroupe.TD));

        List<Groupe> result = dao.findAll();

        assertTrue(result.stream().anyMatch(g -> g.getCode().equals(CODE_PREFIX + "002")));
        assertTrue(result.stream().anyMatch(g -> g.getCode().equals(CODE_PREFIX + "003")));
    }

    @Test
    @DisplayName("findById() avec un ID inexistant → retourne null")
    void findById_id_inexistant_retourne_null() {
        Groupe result = dao.findById(Long.MAX_VALUE);
        assertNull(result, "Un ID inexistant doit retourner null");
    }

    @Test
    @DisplayName("findByNiveau()  retourne uniquement les groupes du niveau demandé")
    void findByNiveau_filtre_correctement_par_niveau() {
        dao.save(buildGroupe(CODE_PREFIX + "004", "L1 TP1", NiveauGroupe.L1, TypeGroupe.TP));
        dao.save(buildGroupe(CODE_PREFIX + "005", "L3 TD1", NiveauGroupe.L3, TypeGroupe.TD));

        List<Groupe> l1 = dao.findByNiveau(NiveauGroupe.L1);

        assertTrue(l1.stream().anyMatch(g -> g.getCode().equals(CODE_PREFIX + "004")),
                "Le groupe L1 doit être dans la liste");
        assertFalse(l1.stream().anyMatch(g -> g.getCode().equals(CODE_PREFIX + "005")),
                "Le groupe L3 ne doit PAS être dans la liste L1");
    }

    //UPDATE

    @Test
    @DisplayName("update()  le libellé et l'effectif sont bien mis à jour en base")
    void update_modifie_le_groupe_en_base() {
        dao.save(buildGroupe(CODE_PREFIX + "006", "Ancien libellé", NiveauGroupe.L1, TypeGroupe.TD));
        Long id = trouverIdParCode(CODE_PREFIX + "006");

        Groupe fromDb = dao.findById(id);
        fromDb.setLibelle("Nouveau libellé");
        fromDb.setEffectifMax(60);
        dao.update(fromDb);

        Groupe updated = dao.findById(id);
        assertEquals("Nouveau libellé", updated.getLibelle(), "Le libellé doit être mis à jour");
        assertEquals(60,                 updated.getEffectifMax(), "L'effectif max doit être mis à jour");
    }

    //DELETE

    @Test
    @DisplayName("delete() → supprime définitivement le groupe (hard-delete)")
    void delete_supprime_definitivement_le_groupe() {
        dao.save(buildGroupe(CODE_PREFIX + "007", "A supprimer", NiveauGroupe.L1, TypeGroupe.CM));
        Long id = trouverIdParCode(CODE_PREFIX + "007");
        assertNotNull(id); // pré-condition

        dao.delete(id);

        assertNull(dao.findById(id),
                "Après delete(), findById() doit retourner null (hard-delete)");
    }

    //Creation des groupes

    private Groupe buildGroupe(String code, String libelle, NiveauGroupe niveau, TypeGroupe type) {
        Groupe g = new Groupe();
        g.setCode(code);
        g.setLibelle(libelle);
        g.setNiveau(niveau);
        g.setTypeGroupe(type);
        g.setEffectifMax(30);
        g.setEffectifActuel(0);
        g.setAnneeUniversitaire(2099);
        g.setSyllabus(testSyllabus);
        return g;
    }

    private Long trouverIdParCode(String code) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("SELECT g.id FROM Groupe g WHERE g.code = :code", Long.class)
                    .setParameter("code", code)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}
