package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.enums.NiveauGroupe;
import fr.univ.hyperplanning.model.pedagogie.Groupe;
import fr.univ.hyperplanning.model.pedagogie.SyllabusSemestre;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class GroupeDAO {

    // Liste des Groupes
    public List<Groupe> findAll() {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                    "SELECT g FROM Groupe g LEFT JOIN FETCH g.syllabus ORDER BY g.niveau, g.libelle",
                    Groupe.class).getResultList();
        } finally {
            em.close();
        }
    }

    // Rechercher une Groupe à partir d'un ID
    public Groupe findById(Long id) {
        EntityManager em = JpaUtil.em();
        try {
            return em.find(Groupe.class, id);
        } finally {
            em.close();
        }
    }

    // Rechercher un Groupe à partir d'un niveau de groupe (L1, L2 ...)
    public List<Groupe> findByNiveau(NiveauGroupe niveau) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT g FROM Groupe g WHERE g.niveau = :niveau ORDER BY g.libelle",
                            Groupe.class)
                    .setParameter("niveau", niveau)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Rechercher tous les Syllabus
    public List<SyllabusSemestre> findAllSyllabus() {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                    "SELECT s FROM SyllabusSemestre s ORDER BY s.niveau, s.semestre",
                    SyllabusSemestre.class).getResultList();
        } finally {
            em.close();
        }
    }

    // Créer un Groupe
    public void save(Groupe groupe) {
        EntityManager em = JpaUtil.em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(groupe);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // Mettre à jour un groupe
    public void update(Groupe groupe) {
        EntityManager em = JpaUtil.em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(groupe);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // Supprimer un Groupe
    public void delete(Long id) {
        EntityManager em = JpaUtil.em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Groupe groupe = em.find(Groupe.class, id);
            if (groupe != null) {
                em.remove(groupe);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
