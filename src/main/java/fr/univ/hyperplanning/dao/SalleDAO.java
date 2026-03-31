package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.model.pedagogie.Salle;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class SalleDAO {
    private final EntityManager em;

    public SalleDAO() {
        // Utilisation du chemin exact vers ton JpaUtil et sa méthode em()
        this.em = fr.univ.hyperplanning.infra.config.JpaUtil.em();
    }

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
                // On passe directement les objets LocalDateTime
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .getResultList();
    }
}