package fr.univ.hyperplanning.service;

import fr.univ.hyperplanning.model.ConflictInfo;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.pedagogie.Groupe;
import fr.univ.hyperplanning.model.pedagogie.Salle;
import fr.univ.hyperplanning.model.utilisateurs.Enseignant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
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
                                    "Enseignant X" // À remplacer par c1.getEnseignant().getNom() si dispo
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
     * Détail technique : Logique de comparaison temporelle
     */
    private boolean isOverlapping(Cours c1, Cours c2) {
        // Un conflit existe si le cours 1 ne finit pas avant le début du 2
        // ET que le cours 1 ne commence pas après la fin du 2.
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

    /**
     * MÉTHODE DE RÉSOLUTION (Tâche 5)
     */
    public boolean updateCoursSalle(Long coursId, Long nouvelleSalleId) {
        try {
            em.getTransaction().begin();
            Cours cours = em.find(Cours.class, coursId);
            Salle salle = em.find(Salle.class, nouvelleSalleId);

            if (cours != null && salle != null) {
                cours.setSalle(salle);
                em.merge(cours);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
        }
        return false;
    }
}