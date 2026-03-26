package fr.univ.hyperplanning.infra.config;

import fr.univ.hyperplanning.model.enums.*;
import fr.univ.hyperplanning.model.gestion.*;
import fr.univ.hyperplanning.model.pedagogie.*;
import fr.univ.hyperplanning.model.utilisateurs.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DataInit {

    public static void seed() {
        EntityManager check = JpaUtil.em();
        try {
            // On vérifie si les données de test existent déjà
            Long count = check.createQuery("SELECT COUNT(g) FROM Groupe g WHERE g.code = :code", Long.class)
                    .setParameter("code", "L2TD1")
                    .getSingleResult();
            // Si tu veux forcer la mise à jour, change 0 par 999 temporairement
            if (count > 0) {
                System.out.println("[DataInit] Données déjà présentes.");
                return;
            }
        } finally {
            check.close();
        }

        EntityManager em = JpaUtil.em();
        var tx = em.getTransaction();

        try {
            tx.begin();

            // 1. Salles
            Salle salle1 = findOrCreateSalle(em, "S101", "Bat A");
            Salle salle2 = findOrCreateSalle(em, "S102", "Bat B");

            // 2. Syllabus & Modules
            SyllabusSemestre syllabus = findOrCreateSyllabus(em, "2025-2026", 1, NiveauGroupe.L2);
            ModuleCours mAlgo = findOrCreateModule(em, "INFO201", "Algorithmique", 30, 2.0f, 1, syllabus);
            ModuleCours mBDD = findOrCreateModule(em, "INFO203", "Base de données", 30, 2.0f, 1, syllabus);

            // 3. Groupe
            Groupe g = new Groupe();
            g.setCode("L2TD1");
            g.setLibelle("L2 TD1");
            g.setNiveau(NiveauGroupe.L2);
            g.setTypeGroupe(TypeGroupe.TD);
            g.setAnneeUniversitaire(2025);
            g.setSyllabus(syllabus);
            em.persist(g);

            Date today = Date.valueOf(LocalDate.now());
            Enseignant ens = findOrCreateEnseignant(em, "ens1@univ.fr", today);

            // 4. CRÉATION DU CONFLIT (Même date, Même salle)
            LocalDate demain = LocalDate.now().plusDays(1);

            // Cours 1
            Cours c1 = new Cours();
            c1.setIntitule("Algorithmique - TD");
            c1.setTypeCours(TypeCours.TD);
            c1.setDateDebut(Date.valueOf(demain));
            c1.setDateFin(Date.valueOf(demain));
            c1.setDuree(120);
            c1.setSalle(salle1); // Salle S101
            c1.setModule(mAlgo);
            c1.getGroupes().add(g);
            ens.getCoursEnseignes().add(c1);
            em.persist(c1);

            // Cours 2 -> CONFLIT
            Cours c2 = new Cours();
            c2.setIntitule("Base de données - CM");
            c2.setTypeCours(TypeCours.CM);
            c2.setDateDebut(Date.valueOf(demain));
            c2.setDateFin(Date.valueOf(demain));
            c2.setDuree(90);
            c2.setSalle(salle1); // Même salle S101 !
            c2.setModule(mBDD);
            c2.getGroupes().add(g);
            ens.getCoursEnseignes().add(c2);
            em.persist(c2);

            tx.commit();
            System.out.println("[DataInit] Seed terminé avec conflit généré.");

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // --- Méthodes utilitaires pour éviter le rouge ---

    private static Salle findOrCreateSalle(EntityManager em, String numero, String batiment) {
        try {
            return em.createQuery("SELECT s FROM Salle s WHERE s.numero = :n", Salle.class)
                    .setParameter("n", numero).getSingleResult();
        } catch (NoResultException e) {
            Salle s = new Salle();
            s.setNumero(numero);
            s.setBatiment(batiment);
            s.setCapacite(30);
            s.setActif(true);
            s.setTypeSalle(TypeSalle.SALLE_COURS);
            em.persist(s);
            return s;
        }
    }

    private static SyllabusSemestre findOrCreateSyllabus(EntityManager em, String annee, int sem, NiveauGroupe niv) {
        try {
            return em.createQuery("SELECT s FROM SyllabusSemestre s WHERE s.anneeUniversitaire = :a AND s.semestre = :s AND s.niveau = :n", SyllabusSemestre.class)
                    .setParameter("a", annee).setParameter("s", sem).setParameter("n", niv).getSingleResult();
        } catch (NoResultException e) {
            SyllabusSemestre s = new SyllabusSemestre();
            s.setAnneeUniversitaire(annee);
            s.setSemestre(sem);
            s.setNiveau(niv);
            em.persist(s);
            return s;
        }
    }

    private static ModuleCours findOrCreateModule(EntityManager em, String code, String nom, int h, float c, int s, SyllabusSemestre syl) {
        try {
            return em.createQuery("SELECT m FROM ModuleCours m WHERE m.code = :c", ModuleCours.class)
                    .setParameter("c", code).getSingleResult();
        } catch (NoResultException e) {
            ModuleCours m = new ModuleCours();
            m.setCode(code);
            m.setNom(nom);
            m.setNombreHeures(h);
            m.setCoefficient(c);
            m.setSemestre(s);
            m.setSyllabus(syl);
            em.persist(m);
            return m;
        }
    }

    private static Enseignant findOrCreateEnseignant(EntityManager em, String email, Date d) {
        try {
            return em.createQuery("SELECT e FROM Enseignant e WHERE e.email = :e", Enseignant.class)
                    .setParameter("e", email).getSingleResult();
        } catch (NoResultException e) {
            Enseignant ens = new Enseignant();
            ens.setNom("Dupont");
            ens.setPrenom("Jean");
            ens.setEmail(email);
            ens.setMotDePasseHash("hash_ici"); // Simplifié pour éviter le rouge sur AuthService
            ens.setRole(RoleUtilisateur.ENSEIGNANT);
            ens.setActif(true);
            ens.setDateCreation(d);
            em.persist(ens);
            return ens;
        }
    }
}