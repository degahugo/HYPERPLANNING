package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.enums.StatutAbsence;
import fr.univ.hyperplanning.model.gestion.Absence;
import jakarta.persistence.EntityManager;
import java.util.List;
import fr.univ.hyperplanning.model.gestion.Absence;

public class AbsenceDAO {
    public void sauvegarder(Absence abs) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            em.persist(abs);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public List<Absence> findAbsencesAValider() {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT a FROM Absence a " +
                                    "JOIN FETCH a.etudiantAbsent " +
                                    "JOIN FETCH a.cours " +
                                    "LEFT JOIN FETCH a.piecesJointesPaths " + // Charge les chemins de fichiers
                                    "WHERE a.statut = :s", Absence.class)
                    .setParameter("s", StatutAbsence.DECLAREE)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Absence> findAbsencesByEtudiant(Long etudiantId) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT a FROM Absence a " +
                                    "JOIN FETCH a.cours " +
                                    "WHERE a.etudiantAbsent.id = :id " +
                                    "ORDER BY a.dateAbsence DESC", Absence.class)
                    .setParameter("id", etudiantId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}