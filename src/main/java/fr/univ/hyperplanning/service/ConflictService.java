package fr.univ.hyperplanning.service;

import fr.univ.hyperplanning.model.ConflictInfo;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.pedagogie.Groupe;
import fr.univ.hyperplanning.model.pedagogie.Salle;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.sql.Date; // Utilisation du type SQL pour matcher l'entité Cours
import java.util.ArrayList;
import java.util.List;

public class ConflictService {

    private EntityManager em;

    public ConflictService() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hyperplanningPU");
        this.em = emf.createEntityManager();
    }

    /**
     * RÉPOND À LA TÂCHE 1 : Détecter automatiquement tous les types de conflits
     */
    public List<ConflictInfo> getAllConflicts() {
        List<ConflictInfo> conflicts = new ArrayList<>();

        try {
            // TÂCHE 4 : Récupération depuis la base
            List<Cours> tousLesCours = em.createQuery("SELECT c FROM Cours c", Cours.class).getResultList();

            for (int i = 0; i < tousLesCours.size(); i++) {
                for (int j = i + 1; j < tousLesCours.size(); j++) {
                    Cours c1 = tousLesCours.get(i);
                    Cours c2 = tousLesCours.get(j);

                    if (isOverlapping(c1, c2)) {

                        // Préparation des détails pour la Tâche 3
                        String nomSalle = (c1.getSalle() != null) ? c1.getSalle().getNumero() : "N/A";
                        String libelleGroupe = c1.getGroupes().isEmpty() ? "N/A" : c1.getGroupes().iterator().next().getLibelle();

                        // --- A. CONFLIT DE SALLE ---
                        if (c1.getSalle() != null && c1.getSalle().equals(c2.getSalle())) {
                            conflicts.add(new ConflictInfo(
                                    c1.getId(),
                                    "SALLE",
                                    c1.getIntitule(),
                                    c2.getIntitule(),
                                    nomSalle,
                                    libelleGroupe,
                                    "Enseignant X"
                            ));
                        }

                        // --- B. CONFLIT DE GROUPE ---
                        if (hasCommonGroup(c1, c2)) {
                            conflicts.add(new ConflictInfo(
                                    c1.getId(),
                                    "GROUPE",
                                    c1.getIntitule(),
                                    c2.getIntitule(),
                                    nomSalle,
                                    libelleGroupe,
                                    "Enseignant X"
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conflicts;
    }

    /**
     * RÉPOND À LA TÂCHE 4 : Récupérer uniquement les cours d'une période précise
     */
    public List<Cours> getCoursByPeriode(Date debut, Date fin) {
        try {
            return em.createQuery(
                            "SELECT c FROM Cours c WHERE c.dateDebut <= :fin AND c.dateFin >= :debut",
                            Cours.class)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * RÉPOND À LA TÂCHE 5 : Résolution du conflit (Regroupé)
     */
    public boolean resolveConflict(Long coursId, Long nouvelleSalleId, Date nvDebut, Date nvFin) {
        try {
            em.getTransaction().begin();

            Cours cours = em.find(Cours.class, coursId);

            if (cours != null) {
                // Modification de la Salle
                if (nouvelleSalleId != null) {
                    Salle salle = em.find(Salle.class, nouvelleSalleId);
                    if (salle != null) {
                        cours.setSalle(salle);
                    }
                }

                // Modification de l'Horaire
                if (nvDebut != null && nvFin != null) {
                    cours.setDateDebut(nvDebut);
                    cours.setDateFin(nvFin);
                }

                em.merge(cours);
                em.getTransaction().commit();
                return true;
            }

            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            return false;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Détail technique : Logique de comparaison temporelle
     */
    private boolean isOverlapping(Cours c1, Cours c2) {
        return !c1.getDateDebut().after(c2.getDateFin()) && !c1.getDateFin().before(c2.getDateDebut());
    }

    /**
     * Détail technique : Vérification des groupes communs
     */
    private boolean hasCommonGroup(Cours c1, Cours c2) {
        for (Groupe g1 : c1.getGroupes()) {
            if (c2.getGroupes().contains(g1)) {
                return true;
            }
        }
        return false;
    }
}
