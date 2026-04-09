package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.pedagogie.Salle;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.List;

public class SalleDAO {
    /*private final EntityManager em;

    public SalleDAO(EntityManager em) {
        this.em = em;
    }*/

    /**
     * US 4.1.1 : Récupérer les salles disponibles pour un créneau donné.
     * Cette requête exclut les salles qui ont un cours chevauchant le créneau [debut, fin].
     */
// Dans SalleDAO.java
    public List<Salle> findSallesDisponibles(LocalDateTime debut, LocalDateTime fin) {
        EntityManager em = JpaUtil.em();
        try {
            String hql = "SELECT s FROM Salle s WHERE s.id NOT IN (" +
                    "SELECT c.salle.id FROM Cours c " +
                    "WHERE c.dateDebut < :fin AND c.dateFin > :debut)";

            return em.createQuery(hql, Salle.class)
                    // On convertit LocalDateTime en Timestamp (qui est compatible avec Date)
                    .setParameter("debut", java.sql.Timestamp.valueOf(debut))
                    .setParameter("fin", java.sql.Timestamp.valueOf(fin))
                    .getResultList();
        } finally{
            em.close();
        }
    }


    /**
     * Récupère toutes les salles déclarées dans la BD
     * @return Liste avec les salles en utilisant toString
     */
    public List<Salle> findAll(){
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("SELECT s FROM Salle s WHERE s.actif = true ORDER BY s.numero", Salle.class).getResultList();
        } finally {
            em.close();
        }
    }

    // Récuperer les numéros de salle
    public List<String> findAllNumeros(){
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("SELECT DISTINCT s.numero FROM Salle s WHERE s.actif = true ORDER BY s.numero", String.class).getResultList();
        } finally {
            em.close();
        }
    }

    // Récuperer une salle par son numéro
    public Salle findByNumero(String numero) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("SELECT s FROM Salle s WHERE s.numero = :numero", Salle.class).setParameter("numero", numero).getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    // Rechercher une salle par son ID
    public Salle findById(Long id) {
        EntityManager em = JpaUtil.em();
        try {
            return em.find(Salle.class, id);
        } finally {
            em.close();
        }
    }

    // Créer une salle (CREATE)
    public void save(Salle salle) {
        EntityManager em = JpaUtil.em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(salle);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // Mettre à jour une salle (UPDATE)
    public void update(Salle salle) {
        EntityManager em = JpaUtil.em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(salle);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // Supprimer une salle (DELETE)
    public void delete(Long id) {
        EntityManager em = JpaUtil.em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Salle salle = em.find(Salle.class, id);
            if (salle != null) {
                salle.setActif(false);
                em.merge(salle);
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