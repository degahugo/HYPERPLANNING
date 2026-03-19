package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.utilisateurs.Enseignant;
import jakarta.persistence.EntityManager;

import java.util.List;

public class EnseignantDAO {

    public List<Enseignant> findAll() {

        EntityManager em = JpaUtil.em();

        try {
            return em.createQuery("SELECT e FROM Enseignant e", Enseignant.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Enseignant findById(Long id){

        EntityManager em = JpaUtil.em();

        try {
            return em.find(Enseignant.class, id);
        } finally {
            em.close();
        }
    }
}