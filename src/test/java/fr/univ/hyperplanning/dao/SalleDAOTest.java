package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.enums.TypeSalle;
import fr.univ.hyperplanning.model.pedagogie.Salle;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SalleDAO – CRUD complet")
class SalleDAOTest {

    // Préfixe unique pour identifier les salles de test et ne pas toucher aux données seed
    private static final String PREFIX = "TEST_S_";

    private final SalleDAO dao = new SalleDAO();

    // Nettoyage AVANT (si crash précédent) ET APRÈS chaque test
    @BeforeEach
    @AfterEach
    void nettoyerSallesDeTest() {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            List<Salle> aSupprimer = em.createQuery(
                            "SELECT s FROM Salle s WHERE s.numero LIKE :prefix", Salle.class)
                    .setParameter("prefix", PREFIX + "%")
                    .getResultList();
            for (Salle s : aSupprimer) {
                em.remove(em.contains(s) ? s : em.merge(s));
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
    @DisplayName("save()  la salle est persistée et retrouvable par son numéro")
    void save_persiste_salle_retrouvable_par_numero() {
        Salle salle = buildSalle(PREFIX + "101", "Bat A", 30);

        dao.save(salle);

        Salle fromDb = dao.findByNumero(PREFIX + "101");
        assertNotNull(fromDb, "La salle doit être retrouvable après save()");
        assertEquals(PREFIX + "101", fromDb.getNumero());
        assertEquals("Bat A",        fromDb.getBatiment());
        assertEquals(30,             fromDb.getCapacite());
        assertTrue(fromDb.isActif(), "La salle créée doit être active");
    }

    //READ

    @Test
    @DisplayName("findAll()  retourne uniquement les salles actives (actif = true)")
    void findAll_retourne_seulement_salles_actives() {
        Salle active   = buildSalle(PREFIX + "102", "Bat B", 20);
        Salle inactive = buildSalle(PREFIX + "103", "Bat C", 15);
        inactive.setActif(false);

        dao.save(active);
        dao.save(inactive);

        List<Salle> result = dao.findAll();

        assertTrue(result.stream().anyMatch(s -> s.getNumero().equals(PREFIX + "102")),
                "La salle ACTIVE doit apparaître dans findAll()");
        assertFalse(result.stream().anyMatch(s -> s.getNumero().equals(PREFIX + "103")),
                "La salle INACTIVE ne doit PAS apparaître dans findAll()");
    }

    @Test
    @DisplayName("findById() retourne exactement la bonne salle")
    void findById_retourne_la_bonne_salle() {
        dao.save(buildSalle(PREFIX + "104", "Bat D", 25));
        Long id = dao.findByNumero(PREFIX + "104").getId();

        Salle fromDb = dao.findById(id);

        assertNotNull(fromDb);
        assertEquals(PREFIX + "104", fromDb.getNumero());
    }

    @Test
    @DisplayName("findByNumero() avec un numéro inconnu → retourne null")
    void findByNumero_inconnu_retourne_null() {
        Salle result = dao.findByNumero("NUMERO_INEXISTANT_XYZ_999");
        assertNull(result, "Un numéro inconnu doit retourner null");
    }

    //UPDATE

    @Test
    @DisplayName("update()  les nouvelles valeurs sont bien persistées en base")
    void update_modifie_les_champs_en_base() {
        dao.save(buildSalle(PREFIX + "105", "Bat E", 40));
        Salle fromDb = dao.findByNumero(PREFIX + "105");

        fromDb.setCapacite(80);
        fromDb.setBatiment("Bat Z");
        dao.update(fromDb);

        Salle updated = dao.findByNumero(PREFIX + "105");
        assertEquals(80,      updated.getCapacite(), "La capacité doit être mise à jour");
        assertEquals("Bat Z", updated.getBatiment(),  "Le bâtiment doit être mis à jour");
    }

    //DELETE

    @Test
    @DisplayName("delete() désactive la salle (soft-delete) sans la supprimer physiquement")
    void delete_desactive_salle_soft_delete() {
        dao.save(buildSalle(PREFIX + "106", "Bat F", 50));
        Long id = dao.findByNumero(PREFIX + "106").getId();

        dao.delete(id);

        // La salle existe encore en base (soft-delete)
        Salle afterDelete = dao.findById(id);
        assertNotNull(afterDelete, "La salle doit toujours exister physiquement en base");
        assertFalse(afterDelete.isActif(), "La salle doit être marquée inactive (actif = false)");

        // Mais elle ne remonte plus dans findAll()
        assertFalse(dao.findAll().stream().anyMatch(s -> s.getId().equals(id)),
                "La salle désactivée ne doit plus apparaître dans findAll()");
    }

    // Creation de salles

    private Salle buildSalle(String numero, String batiment, int capacite) {
        Salle s = new Salle();
        s.setNumero(numero);
        s.setBatiment(batiment);
        s.setCapacite(capacite);
        s.setTypeSalle(TypeSalle.SALLE_COURS);
        s.setActif(true);
        return s;
    }
}
