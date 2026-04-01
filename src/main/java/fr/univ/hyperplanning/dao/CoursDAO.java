package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.model.enums.NiveauGroupe;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CoursDAO {

    public List<Cours> findAll() {
        EntityManager em = JpaUtil.em();
        List<Cours> cours = em.createQuery(
                "SELECT DISTINCT c FROM Cours c LEFT JOIN FETCH c.groupes",
                Cours.class
        ).getResultList();
        em.close();
        return cours;
    }

    public List<Cours> findByDateRange(LocalDate start, LocalDate end) {
        EntityManager em = JpaUtil.em();
        LocalDateTime startDate = start.atStartOfDay();
        LocalDateTime endDate = end.atTime(23, 59, 59);
        List<Cours> cours = em.createQuery(
                        "SELECT DISTINCT c FROM Cours c LEFT JOIN FETCH c.groupes " +
                                "WHERE c.dateDebut BETWEEN :start AND :end",
                        Cours.class)
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .getResultList();
        em.close();
        return cours;
    }

    public List<Cours> findByDay(LocalDate date) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("""
            SELECT DISTINCT c FROM Cours c
            LEFT JOIN FETCH c.groupes
            WHERE c.dateDebut BETWEEN :start AND :end
            """, Cours.class)
                    .setParameter("start", date.atStartOfDay())
                    .setParameter("end",   date.atTime(23, 59, 59))
                    .getResultList();
        } finally { em.close(); }
    }


    public List<Cours> findByWeek(LocalDate date) {

        LocalDate start = date.with(java.time.DayOfWeek.MONDAY);
        LocalDate end = date.with(java.time.DayOfWeek.SUNDAY);

        return findByDateRange(start, end);
    }

    public List<Cours> findByMonth(LocalDate date) {

        LocalDate start = date.withDayOfMonth(1);
        LocalDate end = date.withDayOfMonth(date.lengthOfMonth());

        return findByDateRange(start, end);
    }

    public List<String> findAllGroupCodes() {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("""
                SELECT g.code
                FROM Groupe g
                ORDER BY g.code
                """, String.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<String> findGroupCodesByPromotion(NiveauGroupe niveau) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("""
                SELECT g.code
                FROM Groupe g
                WHERE g.niveau = :niveau
                ORDER BY g.code
                """, String.class)
                    .setParameter("niveau", niveau)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Cours> findByEtudiant(Long etudiantId, LocalDate start, LocalDate end) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("""
            SELECT DISTINCT c FROM Cours c
            LEFT JOIN FETCH c.groupes
            JOIN c.groupes g
            JOIN g.etudiants e
            WHERE e.id = :etudiantId
              AND c.dateDebut BETWEEN :start AND :end
            ORDER BY c.dateDebut
            """, Cours.class)
                    .setParameter("etudiantId", etudiantId)
                    .setParameter("start", start.atStartOfDay())
                    .setParameter("end", end.atTime(23, 59, 59))
                    .getResultList();
        } finally { em.close(); }
    }

    public List<Cours> findAllByEtudiant(Long etudiantId) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("""
            SELECT DISTINCT c FROM Cours c
            LEFT JOIN FETCH c.groupes
            JOIN c.groupes g
            JOIN g.etudiants e
            WHERE e.id = :etudiantId
            """, Cours.class)
                    .setParameter("etudiantId", etudiantId)
                    .getResultList();
        } finally { em.close(); }
    }

    // Cours d'un enseignant (via enseignant_cours)
    public List<Cours> findByEnseignant(Long enseignantId, LocalDate start, LocalDate end) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("""
            SELECT DISTINCT c FROM Enseignant ens
            JOIN ens.coursEnseignes c
            LEFT JOIN FETCH c.groupes
            WHERE ens.id = :enseignantId
              AND c.dateDebut BETWEEN :start AND :end
            ORDER BY c.dateDebut
            """, Cours.class)
                    .setParameter("enseignantId", enseignantId)
                    .setParameter("start", start.atStartOfDay())
                    .setParameter("end",   end.atTime(23, 59, 59))
                    .getResultList();
        } finally { em.close(); }
    }

    public List<Cours> findAllByEnseignant(Long enseignantId) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("""
            SELECT DISTINCT c FROM Enseignant ens
            JOIN ens.coursEnseignes c
            LEFT JOIN FETCH c.groupes
            WHERE ens.id = :enseignantId
            """, Cours.class)
                    .setParameter("enseignantId", enseignantId)
                    .getResultList();
        } finally { em.close(); }
    }

    public List<Cours> findByIntervenant(Long intervenantId, LocalDate start, LocalDate end) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("""
            SELECT DISTINCT c FROM Intervenant inv
            JOIN inv.coursInterventions c
            LEFT JOIN FETCH c.groupes
            WHERE inv.id = :intervenantId
              AND c.dateDebut BETWEEN :start AND :end
            ORDER BY c.dateDebut
            """, Cours.class)
                    .setParameter("intervenantId", intervenantId)
                    .setParameter("start", start.atStartOfDay())
                    .setParameter("end",   end.atTime(23, 59, 59))
                    .getResultList();
        } finally { em.close(); }
    }

    public List<Cours> findAllByIntervenant(Long intervenantId) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("""
            SELECT DISTINCT c FROM Intervenant inv
            JOIN inv.coursInterventions c
            LEFT JOIN FETCH c.groupes
            WHERE inv.id = :intervenantId
            """, Cours.class)
                    .setParameter("intervenantId", intervenantId)
                    .getResultList();
        } finally { em.close(); }
    }

    // Récuperer les cours d'une salle pour une période donnée
    public List<Cours> findBySalle(Long salleId, LocalDate debut, LocalDate fin) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("""
            SELECT DISTINCT c FROM Cours c
            LEFT JOIN FETCH c.groupes
            WHERE c.salle.id = :salleId
              AND c.dateDebut BETWEEN :debut AND :fin
            ORDER BY c.dateDebut
            """, Cours.class)
                    .setParameter("salleId", salleId)
                    .setParameter("debut", debut.atStartOfDay())
                    .setParameter("fin", fin.atTime(23, 59, 59))
                    .getResultList();
        } finally {
            em.close();
        }
    }

}