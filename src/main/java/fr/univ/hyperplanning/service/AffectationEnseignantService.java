package fr.univ.hyperplanning.service;

import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.utilisateurs.Enseignant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class AffectationEnseignantService {

    public void affecterEnseignantsACours(Long coursId, List<Long> enseignantIds) {
        EntityManager em = JpaUtil.em();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Cours cours = em.find(Cours.class, coursId);
            if (cours == null) {
                throw new IllegalArgumentException("Cours introuvable avec l'id : " + coursId);
            }

            // On enlève d’abord ce cours de tous les enseignants
            List<Enseignant> tousLesEnseignants = em.createQuery(
                    "SELECT e FROM Enseignant e",
                    Enseignant.class
            ).getResultList();

            for (Enseignant enseignant : tousLesEnseignants) {
                enseignant.getCoursEnseignes().remove(cours);
            }

            // On ajoute le cours seulement aux enseignants choisis
            for (Long enseignantId : enseignantIds) {
                Enseignant enseignant = em.find(Enseignant.class, enseignantId);
                if (enseignant != null) {
                    enseignant.getCoursEnseignes().add(cours);
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Enseignant> getEnseignantsByCours(Long coursId) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT e FROM Enseignant e JOIN e.coursEnseignes c WHERE c.id = :coursId ORDER BY e.nom, e.prenom",
                            Enseignant.class
                    ).setParameter("coursId", coursId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}