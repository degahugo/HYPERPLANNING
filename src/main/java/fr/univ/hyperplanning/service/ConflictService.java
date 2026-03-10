package fr.univ.hyperplanning.service;

import fr.univ.hyperplanning.model.ConflictInfo;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.ArrayList;
import java.util.List;

public class ConflictService {

    private EntityManager em;

    public ConflictService() {
        // Connexion via l'unité de persistance du groupe
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hyperplanningPU");
        this.em = emf.createEntityManager();
    }

    public List<ConflictInfo> getRoomConflicts() {
        List<ConflictInfo> conflicts = new ArrayList<>();

        try {
            // Récupération des objets Cours via Hibernate
            List<Cours> tousLesCours = em.createQuery("SELECT c FROM Cours c", Cours.class)
                    .getResultList();


            for (int i = 0; i < tousLesCours.size(); i++) {
                for (int j = i + 1; j < tousLesCours.size(); j++) {
                    Cours c1 = tousLesCours.get(i);
                    Cours c2 = tousLesCours.get(j);

                    // 1. Vérification de la salle (nom de la méthode générée par Lombok)
                    if (c1.getSalle() != null && c1.getSalle().equals(c2.getSalle())) {

                        // 2. Chevauchement horaire (Adapté au type java.sql.Date)
                        // Remplace la ligne du "if" temporel par celle-ci :
                        if (!c1.getDateDebut().after(c2.getDateFin()) && !c1.getDateFin().before(c2.getDateDebut())) {

                            conflicts.add(new ConflictInfo(
                                    c1.getId(),
                                    "Conflit salle " + c1.getSalle().getNumero() + " : " +
                                            c1.getIntitule() + " et " + c2.getIntitule()
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des conflits : " + e.getMessage());
            e.printStackTrace();
        }
        return conflicts;
    }
}