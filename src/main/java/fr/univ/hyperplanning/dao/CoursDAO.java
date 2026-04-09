package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.enums.NiveauGroupe;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CoursDAO {



    public List<Cours> findAll() {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                    "SELECT DISTINCT c FROM Cours c " +
                            "LEFT JOIN FETCH c.groupes " +
                            "LEFT JOIN FETCH c.enseignantsEnseignes " +
                            "LEFT JOIN FETCH c.intervenantsInterventions",
                    Cours.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Cours> findByDateRange(LocalDate start, LocalDate end) {
        EntityManager em = JpaUtil.em();
        LocalDateTime startDate = start.atStartOfDay();
        LocalDateTime endDate = end.atTime(23, 59, 59);
        try {
            return em.createQuery(
                            "SELECT DISTINCT c FROM Cours c " +
                                    "LEFT JOIN FETCH c.groupes " +
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



    public List<Cours> findByEtudiant(Long etudiantId, LocalDate start, LocalDate end) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT DISTINCT c FROM Cours c " +
                                    "LEFT JOIN FETCH c.groupes " +
                                    "LEFT JOIN FETCH c.enseignantsEnseignes " +
                                    "LEFT JOIN FETCH c.intervenantsInterventions " +
                                    "JOIN c.groupes g JOIN g.etudiants e " +
                                    "WHERE e.id = :etudiantId AND c.dateDebut BETWEEN :start AND :end " +
                                    "ORDER BY c.dateDebut", Cours.class)
                    .setParameter("etudiantId", etudiantId)
                    .setParameter("start", start.atStartOfDay())
                    .setParameter("end", end.atTime(23, 59, 59))
                    .getResultList();
        } finally { em.close(); }
    }

    public List<Cours> findByEnseignant(Long enseignantId, LocalDate start, LocalDate end) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT DISTINCT c FROM Cours c " +
                                    "LEFT JOIN FETCH c.groupes " +
                                    "LEFT JOIN FETCH c.enseignantsEnseignes " +
                                    "LEFT JOIN FETCH c.intervenantsInterventions " +
                                    "JOIN c.enseignantsEnseignes ens " +
                                    "WHERE ens.id = :enseignantId AND c.dateDebut BETWEEN :start AND :end " +
                                    "ORDER BY c.dateDebut", Cours.class)
                    .setParameter("enseignantId", enseignantId)
                    .setParameter("start", start.atStartOfDay())
                    .setParameter("end", end.atTime(23, 59, 59))
                    .getResultList();
        } finally { em.close(); }
    }

    public List<Cours> findByIntervenant(Long intervenantId, LocalDate start, LocalDate end) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT DISTINCT c FROM Cours c " +
                                    "LEFT JOIN FETCH c.groupes " +
                                    "LEFT JOIN FETCH c.enseignantsEnseignes " +
                                    "LEFT JOIN FETCH c.intervenantsInterventions " +
                                    "JOIN c.intervenantsInterventions inv " +
                                    "WHERE inv.id = :intervenantId AND c.dateDebut BETWEEN :start AND :end " +
                                    "ORDER BY c.dateDebut", Cours.class)
                    .setParameter("intervenantId", intervenantId)
                    .setParameter("start", start.atStartOfDay())
                    .setParameter("end", end.atTime(23, 59, 59))
                    .getResultList();
        } finally { em.close(); }
    }



    public List<Cours> findAllByEtudiant(Long etudiantId) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT DISTINCT c FROM Cours c JOIN c.groupes g JOIN g.etudiants e WHERE e.id = :id", Cours.class)
                    .setParameter("id", etudiantId).getResultList();
        } finally { em.close(); }
    }

    public List<Cours> findAllByEnseignant(Long enseignantId) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT DISTINCT c FROM Cours c JOIN c.enseignantsEnseignes e WHERE e.id = :id", Cours.class)
                    .setParameter("id", enseignantId).getResultList();
        } finally { em.close(); }
    }

    public List<Cours> findAllByIntervenant(Long intervenantId) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT DISTINCT c FROM Cours c JOIN c.intervenantsInterventions i WHERE i.id = :id", Cours.class)
                    .setParameter("id", intervenantId).getResultList();
        } finally { em.close(); }
    }

    // -

    public List<Cours> findByDay(LocalDate date) { return findByDateRange(date, date); }

    public List<Cours> findByWeek(LocalDate date) {
        return findByDateRange(date.with(java.time.DayOfWeek.MONDAY), date.with(java.time.DayOfWeek.SUNDAY));
    }

    public List<Cours> findByMonth(LocalDate date) {
        return findByDateRange(date.withDayOfMonth(1), date.withDayOfMonth(date.lengthOfMonth()));
    }

    //

    public List<String> findAllGroupCodes() {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("SELECT g.code FROM Groupe g ORDER BY g.code", String.class).getResultList();
        } finally { em.close(); }
    }

    public List<String> findGroupCodesByPromotion(NiveauGroupe niveau) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("SELECT g.code FROM Groupe g WHERE g.niveau = :niveau ORDER BY g.code", String.class)
                    .setParameter("niveau", niveau).getResultList();
        } finally { em.close(); }
    }

    public List<Cours> findBySalle(Long salleId, LocalDate debut, LocalDate fin) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT DISTINCT c FROM Cours c LEFT JOIN FETCH c.groupes WHERE c.salle.id = :salleId AND c.dateDebut BETWEEN :debut AND :fin",
                            Cours.class)
                    .setParameter("salleId", salleId)
                    .setParameter("debut", debut.atStartOfDay())
                    .setParameter("fin", fin.atTime(23, 59, 59))
                    .getResultList();
        } finally { em.close(); }
    }

    /**
     * Prends un cours en entrée et l'insert dans la base de données
     @param cours
     @version 1
     @author Dylan MARTIN
     */
    public void nouveauCours(Cours cours) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            em.merge(cours);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /*public void modificationCours(Cours cours, ) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            Cours c =  em.find(Cours.class, cours.getId());
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }*/
}