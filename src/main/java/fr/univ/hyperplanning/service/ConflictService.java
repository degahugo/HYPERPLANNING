package fr.univ.hyperplanning.service;

import fr.univ.hyperplanning.model.ConflictInfo;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.pedagogie.Groupe;
import fr.univ.hyperplanning.model.pedagogie.Salle;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.time.LocalDateTime;
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
            List<fr.univ.hyperplanning.model.pedagogie.Cours> tousLesCours =
                    em.createQuery("SELECT c FROM Cours c", fr.univ.hyperplanning.model.pedagogie.Cours.class).getResultList();

            // --- TES LOGS DE DEBUG (CONSERVÉS) ---
            System.out.println("======= DEBUG CONFLITS =======");
            if (tousLesCours == null || tousLesCours.isEmpty()) {
                System.out.println("[ALERTE] La liste est VIDE. Le DataInit n'a rien enregistré !");
            } else {
                System.out.println("[OK] Nombre de cours trouvés : " + tousLesCours.size());
                for (fr.univ.hyperplanning.model.pedagogie.Cours c : tousLesCours) {
                    String salleNom = (c.getSalle() != null) ? c.getSalle().getNumero() : "SANS SALLE";
                    System.out.println(" > ID: " + c.getId() + " | " + c.getIntitule() + " | Salle: " + salleNom);
                }
            }
            System.out.println("==============================");

            // --- DÉBUT DE LA TRANSACTION ---
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }

            for (int i = 0; i < tousLesCours.size(); i++) {
                for (int j = i + 1; j < tousLesCours.size(); j++) {
                    Cours c1 = tousLesCours.get(i);
                    Cours c2 = tousLesCours.get(j);

                    if (isOverlapping(c1, c2)) {

                        // On prépare les infos pour l'admin
                        String nomSalle = (c1.getSalle() != null) ? c1.getSalle().getNumero() : "N/A";
                        String libelleGroupe = c1.getGroupes().isEmpty() ? "N/A" : c1.getGroupes().iterator().next().getLibelle();

                        // --- VÉRIFICATION CONFLIT DE SALLE ---
                        if (c1.getSalle() != null && c1.getSalle().equals(c2.getSalle())) {

                            // ON MASQUE LES COURS (C'est ici que le conflit de salle agit)
                            c1.setEnConflit(true);
                            c2.setEnConflit(true);
                            em.merge(c1);
                            em.merge(c2);

                            conflicts.add(new ConflictInfo(
                                    c1.getId(), "SALLE", c1.getIntitule(), c2.getIntitule(),
                                    nomSalle, libelleGroupe, "Enseignant X"
                            ));
                        }

                        // --- VÉRIFICATION CONFLIT DE GROUPE ---
                        if (hasCommonGroup(c1, c2)) {

                            // ON MASQUE AUSSI (Si un groupe a deux cours en même temps)
                            c1.setEnConflit(true);
                            c2.setEnConflit(true);
                            em.merge(c1);
                            em.merge(c2);

                            conflicts.add(new ConflictInfo(
                                    c1.getId(), "GROUPE", c1.getIntitule(), c2.getIntitule(),
                                    nomSalle, libelleGroupe, "Enseignant X"
                            ));
                        }
                    }
                }
            }

            // On valide les changements de statut (enConflit = true)
            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
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
    public boolean resolveConflict(Long coursId, Long nouvelleSalleId, LocalDateTime nvDebut, LocalDateTime nvFin){
        try {
            em.getTransaction().begin();

            // 1. On récupère le cours à modifier
            Cours cours = em.find(Cours.class, coursId);

            if (cours != null) {
                // 2. Si une nouvelle salle est fournie
                if (nouvelleSalleId != null) {
                    Salle nouvelleSalle = em.find(Salle.class, nouvelleSalleId);
                    if (nouvelleSalle != null) {
                        // On change la salle
                        cours.setSalle(nouvelleSalle);

                        // IMPORTANT : On force les horaires du cours pour qu'ils
                        // correspondent au créneau de disponibilité trouvé dans cette salle
                        if (nvDebut != null && nvFin != null) {
                            cours.setDateDebut(nvDebut);
                            cours.setDateFin(nvFin);
                            System.out.println("[DEBUG] Salle modifiée : " + nouvelleSalle.getNumero() +
                                    " avec report des horaires : " + nvDebut + " à " + nvFin);
                        }
                    }
                } else {
                    // Si on change seulement l'horaire sans changer la salle
                    if (nvDebut != null && nvFin != null) {
                        cours.setDateDebut(nvDebut);
                        cours.setDateFin(nvFin);
                    }
                }


                // --- L'ÉTAPE CRUCIALE ---
                // Le conflit est résolu, on rend le cours de nouveau VISIBLE
                cours.setEnConflit(false);
                System.out.println("[OK] Cours ID " + coursId + " marqué comme résolu et visible.");

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
        // Debug pour voir ce que le service compare réellement
        // System.out.println("Compare " + c1.getIntitule() + " [" + c1.getDateDebut() + "] avec " + c2.getIntitule());

        if (c1.getDateDebut() == null || c1.getDateFin() == null ||
                c2.getDateDebut() == null || c2.getDateFin() == null) return false;

        // Un conflit existe si le début de l'un est avant la fin de l'autre (et inversement)
        return c1.getDateDebut().isBefore(c2.getDateFin()) &&
                c2.getDateDebut().isBefore(c1.getDateFin());
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

