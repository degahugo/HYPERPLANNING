package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CoursDAO {

    // Récupérer tous les cours sans exception
    public List<Cours> findAll() {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("SELECT DISTINCT c FROM Cours c LEFT JOIN FETCH c.enseignantsEnseignes LEFT JOIN FETCH c.intervenantsInterventions", Cours.class).getResultList();
        } finally {
            em.close();
        }
    }

    // Récupérer les cours sur une période donnée (Admin / Vue globale)
    public List<Cours> findByDateRange(LocalDate start, LocalDate end) {
        EntityManager em = JpaUtil.em();
        LocalDateTime startDate = start.atStartOfDay();
        LocalDateTime endDate = end.atTime(23, 59, 59);
        try {
            return em.createQuery(
                            "SELECT DISTINCT c FROM Cours c " +
                                    "LEFT JOIN FETCH c.enseignantsEnseignes " +
                                    "LEFT JOIN FETCH c.intervenantsInterventions " +
                                    "WHERE c.dateDebut BETWEEN :start AND :end",
                            Cours.class)
                    .setParameter("start", startDate)
                    .setParameter("end", endDate)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    //  Récupérer les cours d'un étudiant via ses groupes
    public List<Cours> findByEtudiant(Long etudiantId, LocalDate start, LocalDate end) {
        EntityManager em = JpaUtil.em();
        LocalDateTime startDate = start.atStartOfDay();
        LocalDateTime endDate = end.atTime(23, 59, 59);
        try {
            return em.createQuery(
                            "SELECT DISTINCT c FROM Cours c " +
                                    "LEFT JOIN FETCH c.enseignantsEnseignes " +
                                    "LEFT JOIN FETCH c.intervenantsInterventions " +
                                    "JOIN c.groupes g JOIN g.etudiants e " +
                                    "WHERE e.id = :etudiantId AND c.dateDebut BETWEEN :start AND :end",
                            Cours.class)
                    .setParameter("etudiantId", etudiantId)
                    .setParameter("start", startDate)
                    .setParameter("end", endDate)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Récupérer les cours où un enseignant intervient
    public List<Cours> findByEnseignant(Long enseignantId, LocalDate start, LocalDate end) {
        EntityManager em = JpaUtil.em();
        LocalDateTime startDate = start.atStartOfDay();
        LocalDateTime endDate = end.atTime(23, 59, 59);
        try {
            return em.createQuery(
                            "SELECT DISTINCT c FROM Cours c " +
                                    "LEFT JOIN FETCH c.enseignantsEnseignes " +
                                    "LEFT JOIN FETCH c.intervenantsInterventions " +
                                    "JOIN c.enseignantsEnseignes e " +
                                    "WHERE e.id = :enseignantId AND c.dateDebut BETWEEN :start AND :end",
                            Cours.class)
                    .setParameter("enseignantId", enseignantId)
                    .setParameter("start", startDate)
                    .setParameter("end", endDate)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    //  Récupérer les cours pour un intervenant extérieur
    public List<Cours> findByIntervenant(Long intervenantId, LocalDate start, LocalDate end) {
        EntityManager em = JpaUtil.em();
        LocalDateTime startDate = start.atStartOfDay();
        LocalDateTime endDate = end.atTime(23, 59, 59);
        try {
            return em.createQuery(
                            "SELECT DISTINCT c FROM Cours c " +
                                    "LEFT JOIN FETCH c.enseignantsEnseignes " +
                                    "LEFT JOIN FETCH c.intervenantsInterventions " +
                                    "JOIN c.intervenantsInterventions i " +
                                    "WHERE i.id = :intervenantId AND c.dateDebut BETWEEN :start AND :end",
                            Cours.class)
                    .setParameter("intervenantId", intervenantId)
                    .setParameter("start", startDate)
                    .setParameter("end", endDate)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Récupérer les cours pour un jour précis
    public List<Cours> findByDay(LocalDate date) {
        return findByDateRange(date, date);
    }

    // Récupérer les cours pour une semaine complète
    public List<Cours> findByWeek(LocalDate date) {
        LocalDate start = date.with(java.time.DayOfWeek.MONDAY);
        LocalDate end = date.with(java.time.DayOfWeek.SUNDAY);
        return findByDateRange(start, end);
    }

    // Récupérer les cours pour un mois complet
    public List<Cours> findByMonth(LocalDate date) {
        LocalDate start = date.withDayOfMonth(1);
        LocalDate end = date.withDayOfMonth(date.lengthOfMonth());
        return findByDateRange(start, end);
    }
}