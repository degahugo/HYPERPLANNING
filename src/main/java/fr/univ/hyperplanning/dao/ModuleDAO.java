package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.pedagogie.ModuleCours;
import jakarta.persistence.EntityManager;
import java.util.List;

public class ModuleDAO {

    /**
     * Récupère la liste de tous les modules enregistrés en base de données.
     * Utile pour remplir la ComboBox dans le formulaire d'ajout de cours.
     */
    public List<ModuleCours> findAll() {
        EntityManager em = JpaUtil.em();
        try {
            // Requête JPQL simple pour récupérer toutes les entités ModuleCours
            return em.createQuery("SELECT m FROM ModuleCours m", ModuleCours.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        } finally {
            // Très important pour libérer la connexion Postgres
            em.close();
        }
    }

    /**
     * Trouve un module spécifique par son ID.
     */
    public ModuleCours findById(Long id) {
        EntityManager em = JpaUtil.em();
        try {
            return em.find(ModuleCours.class, id);
        } finally {
            em.close();
        }
    }
}