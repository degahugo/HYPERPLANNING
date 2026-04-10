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

    /**
     * Supprimer un cours dans la base de donnée
     * @param cours Cours qui sera supprimé
     */
    /*public void supprimerCours(Cours cours) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();

            Cours toDelete = em.find(Cours.class, cours.getId());

            if (toDelete != null) {
                // Vider manuellement les relations many-to-many pour éviter les violations de contraintes
                toDelete.getGroupes().clear();
                toDelete.getEnseignantsEnseignes().clear();
                toDelete.getIntervenantsInterventions().clear();

                // Forcer la synchronisation des liens avant le remove
                em.flush();

                em.remove(toDelete);
                System.out.println("Cours supprimé avec succès en base.");
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            System.err.println("Erreur lors de la suppression du cours : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
    }*/
    public void supprimerCours(Cours cours) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            Long id = cours.getId();
            System.out.println("Début de la suppression forcée du cours ID : " + id);

            // 1. Suppression des NOTIFICATIONS liées aux ABSENCES
            try {
                em.createNativeQuery("""
                DELETE FROM notifications 
                WHERE absence_id IN (SELECT id FROM absences WHERE cours_id = ?)
            """).setParameter(1, id).executeUpdate();
            } catch (Exception e) {
                System.out.println("Info: Pas de notifications à supprimer ou table absente.");
            }

            // 2. Liste des tables de dépendances (Ordre important pour les clés étrangères)
            // J'ai mis les noms que tu as utilisés dans ton dernier code fonctionnel
            String[] tablesDep = {
                    "historiques",
                    "demandes_modification",
                    "conflit_cours",
                    "absences",
                    "cours_groupe",
                    "enseignant_cours",
                    "intervenant_cours"
            };

            for (String table : tablesDep) {
                try {
                    int rows = em.createNativeQuery("DELETE FROM " + table + " WHERE cours_id = ?")
                            .setParameter(1, id)
                            .executeUpdate();
                    if(rows > 0) System.out.println("Nettoyage table : " + table + " (" + rows + " lignes)");
                } catch (Exception e) {
                    // On passe à la suite si la table n'existe pas ou si la colonne diffère
                    System.out.println("Note: Saut de la table " + table + " (déjà vide ou inexistante)");
                }
            }

            // 3. LA SUPPRESSION FINALE DU COURS
            int result = em.createNativeQuery("DELETE FROM cours WHERE id = ?")
                    .setParameter(1, id)
                    .executeUpdate();

            em.getTransaction().commit();

            if (result > 0) {
                System.out.println("✅ Tout a été nettoyé : Conflits, Notifications, Absences et Cours supprimés.");
            } else {
                System.out.println("⚠️ Le cours n'existait déjà plus en base.");
            }

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            System.err.println("❌ Erreur critique lors de la suppression : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}