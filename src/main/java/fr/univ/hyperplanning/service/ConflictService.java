package fr.univ.hyperplanning.service;

import fr.univ.hyperplanning.model.pedagogie.Groupe;
import fr.univ.hyperplanning.model.ConflictInfo;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.ArrayList;
import java.util.List;
import fr.univ.hyperplanning.model.pedagogie.Salle;

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
            List<Cours> tousLesCours = em.createQuery("SELECT c FROM Cours c", Cours.class)
                    .getResultList();

            for (int i = 0; i < tousLesCours.size(); i++) {
                for (int j = i + 1; j < tousLesCours.size(); j++) {
                    Cours c1 = tousLesCours.get(i);
                    Cours c2 = tousLesCours.get(j);

                    // 1. Vérification de la salle
                    if (c1.getSalle() != null && c1.getSalle().equals(c2.getSalle())) {

                        // 2. Chevauchement horaire (Date identique ou qui se croise)
                        if (!c1.getDateDebut().isAfter(c2.getDateFin()) && !c1.getDateFin().isBefore(c2.getDateDebut())) {

                            // On récupère le libellé du groupe s'il existe
                            String infoGroupe = "Sans groupe";
                            if (c1.getGroupes() != null && !c1.getGroupes().isEmpty()) {
                                // On récupère le premier groupe de la liste
                                Groupe g = c1.getGroupes().iterator().next();
                                infoGroupe = g.getLibelle();
                            }

                            conflicts.add(new ConflictInfo(
                                    c1.getId(),
                                    String.format("Le %s | Salle %s | %s [%s] vs %s",
                                            c1.getDateDebut(),
                                            c1.getSalle().getNumero(),
                                            c1.getIntitule(),
                                            infoGroupe,
                                            c2.getIntitule())
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


/**
 * NOUVELLE MÉTHODE : Permet de résoudre le conflit en changeant la salle d'un cours
 */
public boolean updateCoursSalle(Long coursId, Long nouvelleSalleId) {
    try {
        em.getTransaction().begin();

        // On cherche le cours et la salle par leurs IDs
        Cours cours = em.find(Cours.class, coursId);
        Salle salle = em.find(Salle.class, nouvelleSalleId);

        if (cours != null && salle != null) {
            cours.setSalle(salle); // On modifie la salle en mémoire
            em.merge(cours);       // On synchronise avec la DB
            em.getTransaction().commit(); // On valide la transaction
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