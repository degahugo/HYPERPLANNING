package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.model.pedagogie.Salle;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class SalleDAO {
    private final EntityManager em;

    public SalleDAO(EntityManager em) {
        this.em = em;
    }

    /**
     * US 4.1.1 : Récupérer les salles disponibles pour un créneau donné.
     * Cette requête exclut les salles qui ont un cours chevauchant le créneau [debut, fin].
     */
// Dans SalleDAO.java
    public List<Salle> findSallesDisponibles(LocalDateTime debut, LocalDateTime fin) {
        String hql = "SELECT s FROM Salle s WHERE s.id NOT IN (" +
                "SELECT c.salle.id FROM Cours c " +
                "WHERE c.dateDebut < :fin AND c.dateFin > :debut)";

        return em.createQuery(hql, Salle.class)
                // On convertit LocalDateTime en Timestamp (qui est compatible avec Date)
                .setParameter("debut", java.sql.Timestamp.valueOf(debut))
                .setParameter("fin", java.sql.Timestamp.valueOf(fin))
                .getResultList();
    }
}