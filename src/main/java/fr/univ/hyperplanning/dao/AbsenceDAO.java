package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.enums.StatutAbsence;
import fr.univ.hyperplanning.model.gestion.Absence;
import jakarta.persistence.EntityManager;
import java.util.List;

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
                            "SELECT DISTINCT a FROM Absence a " +
                                    "JOIN FETCH a.utilisateurAbsent " +
                                    "JOIN FETCH a.cours " +
                                    "LEFT JOIN FETCH a.valideePar " +
                                    "LEFT JOIN FETCH a.piecesJointesPaths " +
                                    "WHERE a.statut = :s " +
                                    "ORDER BY a.dateDeclaration DESC", Absence.class)
                    .setParameter("s", StatutAbsence.DECLAREE)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Absence> findAbsencesByUtilisateur(Long utilisateurId) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT a FROM Absence a " +
                                    "JOIN FETCH a.cours " +
                                    "WHERE a.utilisateurAbsent.id = :id " +
                                    "ORDER BY a.dateAbsence DESC", Absence.class)
                    .setParameter("id", utilisateurId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Absence> findAbsencesByEnseignant(Long enseignantId) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT DISTINCT a FROM Absence a " +
                                    "JOIN FETCH a.utilisateurAbsent ua " +
                                    "JOIN FETCH a.cours c " +
                                    "LEFT JOIN c.enseignantsEnseignes e " +
                                    "WHERE e.id = :id " +
                                    "ORDER BY a.dateDeclaration DESC", Absence.class)
                    .setParameter("id", enseignantId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Absence> findAbsencesByIntervenant(Long intervenantId) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT DISTINCT a FROM Absence a " +
                                    "JOIN FETCH a.utilisateurAbsent ua " +
                                    "JOIN FETCH a.cours c " +
                                    "LEFT JOIN c.intervenantsInterventions i " +
                                    "WHERE i.id = :id " +
                                    "ORDER BY a.dateDeclaration DESC", Absence.class)
                    .setParameter("id", intervenantId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Absence> findAllAbsences() {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT DISTINCT a FROM Absence a " +
                                    "JOIN FETCH a.utilisateurAbsent " +
                                    "JOIN FETCH a.cours " +
                                    "LEFT JOIN FETCH a.valideePar " +
                                    "LEFT JOIN FETCH a.piecesJointesPaths " +
                                    "ORDER BY a.dateDeclaration DESC", Absence.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Absence> findAbsencesByStatut(StatutAbsence statut) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT DISTINCT a FROM Absence a " +
                                    "JOIN FETCH a.utilisateurAbsent " +
                                    "JOIN FETCH a.cours " +
                                    "LEFT JOIN FETCH a.valideePar " +
                                    "LEFT JOIN FETCH a.piecesJointesPaths " +
                                    "WHERE a.statut = :statut " +
                                    "ORDER BY a.dateDeclaration DESC", Absence.class)
                    .setParameter("statut", statut)
                    .getResultList();
        } finally {
            em.close();
        }
    }



    public long countAbsencesByUtilisateur(Long utilisateurId) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT COUNT(a) FROM Absence a WHERE a.utilisateurAbsent.id = :id", Long.class)
                    .setParameter("id", utilisateurId)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    public long countAbsencesAValider() {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT COUNT(a) FROM Absence a WHERE a.statut = :s", Long.class)
                    .setParameter("s", StatutAbsence.DECLAREE)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    public long countAbsencesByEnseignant(Long enseignantId) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT COUNT(DISTINCT a) FROM Absence a " +
                                    "JOIN a.cours c " +
                                    "JOIN c.enseignantsEnseignes e " +
                                    "WHERE e.id = :id", Long.class)
                    .setParameter("id", enseignantId)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    public long countAbsencesByIntervenant(Long intervenantId) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT COUNT(DISTINCT a) FROM Absence a " +
                                    "JOIN a.cours c " +
                                    "JOIN c.intervenantsInterventions i " +
                                    "WHERE i.id = :id", Long.class)
                    .setParameter("id", intervenantId)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}