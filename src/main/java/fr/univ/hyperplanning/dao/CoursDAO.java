package fr.univ.hyperplanning.dao;

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
                "SELECT c FROM Cours c",
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
                        "SELECT c FROM Cours c " +
                                "WHERE c.dateDebut BETWEEN :start AND :end",
                        Cours.class
                )
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .getResultList();

        em.close();
        return cours;
    }

    public List<Cours> findByDay(LocalDate date) {

        EntityManager em = JpaUtil.em();

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        List<Cours> cours = em.createQuery(
                        "SELECT c FROM Cours c " +
                                "WHERE c.dateDebut BETWEEN :start AND :end",
                        Cours.class
                )
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        em.close();
        return cours;
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
}