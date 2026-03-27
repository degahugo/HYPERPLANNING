package fr.univ.hyperplanning.infra.config;

import fr.univ.hyperplanning.model.enums.*;
import fr.univ.hyperplanning.model.gestion.*;
import fr.univ.hyperplanning.model.pedagogie.*;
import fr.univ.hyperplanning.model.utilisateurs.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import net.datafaker.Faker;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class DataInit {

    private static final Faker   FAKER = new Faker(Locale.FRENCH);
    private static final Random  RNG   = new Random();

    /*
    seed() données de référence fixes, appelé depuis init() de MainApp
    Rapide, peut être bloquant mais c'est acceptable avent l'affichage
    */

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

<<<<<<< HEAD
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
=======
            // Relations groupe <-> étudiant
            if (!etu.getGroupes().contains(g)) {
                etu.getGroupes().add(g);
                g.getEtudiants().add(etu);
            }


            LocalDate coursDate = LocalDate.now().plusDays(1);

            Cours cours = new Cours();
            cours.setIntitule("Algorithmique - TD");
            cours.setTypeCours(TypeCours.TD);
            cours.setDateDebut(coursDate.atTime(8, 0));
            cours.setDateFin(coursDate.atTime(10, 0));
            cours.setDuree(120);
            cours.setEstCC(false);
            cours.setEstAnnule(false);
            cours.setAnnotation("Séance de TD");
            cours.setSalle(salle);
            cours.setModule(mAlgo);

            cours.getGroupes().add(g);
            g.getCours().add(cours);
            ens.getCoursEnseignes().add(cours);
            inter.getCoursInterventions().add(cours);

            em.persist(cours);

            LocalDate coursDate2 = LocalDate.now().plusDays(2);

            Cours cours2 = new Cours();
            cours2.setIntitule("Algorithmique - CM");
            cours2.setTypeCours(TypeCours.CM);
            cours2.setDateDebut(coursDate2.atTime(9, 0));
            cours2.setDateFin(coursDate2.atTime(10, 30));
            cours2.setDuree(90);
            cours2.setEstCC(false);
            cours2.setEstAnnule(false);
            cours2.setAnnotation("Cours magistral");
            cours2.setSalle(salle2);
            cours2.setModule(mAlgo);

            cours2.getGroupes().add(g);
            g.getCours().add(cours2);

            ens.getCoursEnseignes().add(cours2);

            em.persist(cours2);

            LocalDate coursDate3 = LocalDate.now().plusDays(3);

            Cours cours3 = new Cours();
            cours3.setIntitule("Projet - TD");
            cours3.setTypeCours(TypeCours.TD);
            cours3.setDateDebut(coursDate3.atTime(14, 0));
            cours3.setDateFin(coursDate3.atTime(16, 0));
            cours3.setDuree(120);
            cours3.setEstCC(false);
            cours3.setEstAnnule(false);
            cours3.setAnnotation("Projet tutoré");
            cours3.setSalle(salle2);
            cours3.setModule(mProjet);

            cours3.getGroupes().add(g);
            g.getCours().add(cours3);

            ens.getCoursEnseignes().add(cours3);

            em.persist(cours3);


            Absence absence = new Absence();
            absence.setDateAbsence(Date.valueOf(coursDate));
            absence.setMotif("Maladie");
            absence.setJustificatif(null);
            absence.setStatut(StatutAbsence.DECLAREE);
            absence.setDateDeclaration(LocalDateTime.now());
            absence.setDateValidation(null);
            absence.setMotifRefus(null);
            absence.setCours(cours);
            absence.setEtudiantAbsent(etu);
            absence.setDeclareePar(etu);
            absence.setValideePar(null);
            em.persist(absence);

            etu.getAbsences().add(absence);
            etu.getAbsencesDeclarees().add(absence);


            Notification notif = new Notification();
            notif.setTitre("Absence enregistrée");
            notif.setMessage("Votre absence a bien été enregistrée.");
            notif.setType("ABSENCE");
            notif.setEstLue(false);
            notif.setDateEnvoi(today);
            notif.setDateLecture(today);
            notif.setDestinataire(etu);
            notif.setCours(cours);
            notif.setAbsence(absence);
            notif.setDemandeModification(null);
            em.persist(notif);

            etu.getNotifications().add(notif);


            Historique hist = new Historique();
            hist.setEntiteModifiee("Cours");
            hist.setTypeAction("CREATION");
            hist.setValeurAvant("");
            hist.setValeurApres("Algorithmique - TD");
            hist.setDateModification(today);
            hist.setAdresseIP("127.0.0.1");
            hist.setEffectuePar(admin);
            hist.setCours(cours);
            hist.setSalle(null);
            hist.setGroupe(null);
            em.persist(hist);

            admin.getHistoriques().add(hist);


            Conflit conflit = new Conflit();
            conflit.setTypeConflit("SALLE");
            conflit.setDescription("Conflit de salle potentiel");
            conflit.setDateDetection(today);
            conflit.setEstResolu(false);
            conflit.setDateResolution(today);
            conflit.setSourceSalle(salle);
            conflit.getCoursImpactes().add(cours);
            cours.getConflits().add(conflit);
            em.persist(conflit);

            // =========================
            // 11) DemandeModification
            // =========================
            DemandeModification dm = new DemandeModification();
            dm.setDescription("Demande de déplacement du cours TD Algorithmique");
            dm.setMotif("Déplacement exceptionnel");
            dm.setDateDemande(today);
            dm.setDateTraitement(null);
            dm.setStatut(StatutDemande.EN_ATTENTE);
            dm.setMotifRefus("N/A");
            dm.setCours(cours);
            dm.setDemandeur(ens);
            dm.setTraiteePar(null);
            em.persist(dm);

            ens.getDemandes().add(dm);
>>>>>>> work

            tx.commit();
            System.out.println("[DataInit] Seed terminé avec conflit généré.");

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

<<<<<<< HEAD
    // --- Méthodes utilitaires pour éviter le rouge ---
=======
    /*
    bulk() : insertion dans les tables pour plus de réalisme via la librairie Faker
    appelé depuis un thread daemon dans start() de MainApp start() ne doit appelé uniquement que le FXML
    sous peine de faire planter l'affichage. bulk() n'est jamais appelé depuis seed();
     */

    public static void bulk(int scale) {
        Date today = Date.valueOf(LocalDate.now());
        EntityManager em = JpaUtil.em();
        var tx = em.getTransaction();

        final int BATCH_SIZE = 50;
        int count = 0;

        try {
            tx.begin();

            // 1. Salles
            List<Salle> salles = new ArrayList<>();
            for (int i = 1; i <= Math.max(10, scale / 20); i++) {
                Salle s = ModelFactory.salle(FAKER, i);
                em.persist(s);
                salles.add(s);
                if (++count % BATCH_SIZE == 0) { em.flush(); em.clear(); }
            }

            // 2. Syllabus
            List<SyllabusSemestre> syllabusList = new ArrayList<>();
            for (NiveauGroupe niv : NiveauGroupe.values()) {
                for (int sem = 1; sem <= 2; sem++) {
                    SyllabusSemestre sy = ModelFactory.syllabus(niv, sem);
                    em.persist(sy);
                    syllabusList.add(sy);
                    if (++count % BATCH_SIZE == 0) { em.flush(); em.clear(); }
                }
            }

            // 3. Modules
            List<ModuleCours> modules = new ArrayList<>();
            int modIdx = 1;
            for (SyllabusSemestre sy : syllabusList) {
                sy = em.merge(sy);
                for (int i = 0; i < Math.max(5, scale / 10); i++) {
                    ModuleCours m = ModelFactory.module(FAKER, sy, modIdx++);
                    em.persist(m);
                    modules.add(m);
                    if (++count % BATCH_SIZE == 0) { em.flush(); em.clear(); }
                }
            }

            // 4. Groupes
            List<Groupe> groupes = new ArrayList<>();
            int grpIdx = 1;
            for (int i = 0; i < Math.max(10, scale / 30); i++) {
                SyllabusSemestre sy = em.merge(pick(syllabusList));
                Groupe grp = ModelFactory.groupe(sy, sy.getNiveau(), pick(TypeGroupe.values()), grpIdx++);
                em.persist(grp);
                groupes.add(grp);
                if (++count % BATCH_SIZE == 0) { em.flush(); em.clear(); }
            }

            // 5. Enseignants
            List<Enseignant> enseignants = new ArrayList<>();
            for (int i = 1; i <= Math.max(5, scale / 10); i++) {
                Enseignant e = ModelFactory.enseignant(FAKER, i, today);
                em.persist(e);
                em.persist(ModelFactory.donneePersonnelle(FAKER, e));
                enseignants.add(e);
                if (++count % BATCH_SIZE == 0) { em.flush(); em.clear(); }
            }

            // 6. Intervenants
            for (int i = 1; i <= Math.max(3, scale / 20); i++) {
                Intervenant inter = ModelFactory.intervenant(FAKER, i, today);
                em.persist(inter);
                em.persist(ModelFactory.donneePersonnelle(FAKER, inter));
                if (++count % BATCH_SIZE == 0) { em.flush(); em.clear(); }
            }

            // 7. Administrations
            for (int i = 1; i <= Math.max(2, scale / 50); i++) {
                Administration a = ModelFactory.administration(FAKER, i, today);
                em.persist(a);
                em.persist(ModelFactory.donneePersonnelle(FAKER, a));
                if (++count % BATCH_SIZE == 0) { em.flush(); em.clear(); }
            }

            // 8. Étudiants
            List<Etudiant> etudiants = new ArrayList<>();
            for (int i = 1; i <= scale; i++) {
                Etudiant etu = ModelFactory.etudiant(FAKER, i, today);
                em.persist(etu);
                em.persist(ModelFactory.donneePersonnelle(FAKER, etu));
                etudiants.add(etu);
                Groupe grp = em.merge(pick(groupes));
                etu.getGroupes().add(grp);
                grp.getEtudiants().add(etu);
                grp.setEffectifActuel(grp.getEffectifActuel() + 1);
                if (++count % BATCH_SIZE == 0) { em.flush(); em.clear(); }
            }

            // 9. Cours
            List<Cours> coursList = new ArrayList<>();
            int nbCours = Math.max(20, scale / 2);
            for (int i = 0; i < nbCours; i++) {
                ModuleCours mod = em.merge(pick(modules));
                Salle       sal = em.merge(pick(salles));
                Cours c = ModelFactory.cours(FAKER, mod, sal, pick(TypeCours.values()));
                Groupe grp = em.merge(pick(groupes));
                c.getGroupes().add(grp);
                grp.getCours().add(c);
                em.merge(pick(enseignants)).getCoursEnseignes().add(c);
                em.persist(c);
                coursList.add(c);
                if (++count % BATCH_SIZE == 0) { em.flush(); em.clear(); }
            }

            // 10. Absences
            int nbAbsences = Math.max(10, scale / 3);
            for (int i = 0; i < nbAbsences; i++) {
                Etudiant etu = em.merge(pick(etudiants));
                Cours    co  = em.merge(pick(coursList));
                Absence  a   = ModelFactory.absence(FAKER, co, etu);
                em.persist(a);
                etu.getAbsences().add(a);
                etu.getAbsencesDeclarees().add(a);
                if (++count % BATCH_SIZE == 0) { em.flush(); em.clear(); }
            }
            /*
            // 11. ServiceInformatique
            List<ServiceInformatique> siList = new ArrayList<>();
            for (int i = 1; i <= Math.max(2, scale / 100); i++) {
                ServiceInformatique si = ModelFactory.serviceInformatique(FAKER, i, today);
                em.persist(si);
                em.persist(ModelFactory.donneePersonnelle(FAKER, si));
                siList.add(si);
                if (++count % BATCH_SIZE == 0) { em.flush(); em.clear(); }
            }

            // 12. Notifications
            int nbNotifs = Math.max(10, scale / 4);
            for (int i = 0; i < nbNotifs; i++) {
                Utilisateur dest = em.merge(pick(etudiants));   // destinataire = étudiant aléatoire
                Cours       co  = em.merge(pick(coursList));
                Notification n  = ModelFactory.notification(FAKER, dest, co, today);
                em.persist(n);
                dest.getNotifications().add(n);
                if (++count % BATCH_SIZE == 0) { em.flush(); em.clear(); }
            }

            // 13. Historiques
            // On regroupe admins + enseignants comme auteurs possibles
            List<Utilisateur> auteurs = new ArrayList<>();
            auteurs.addAll(enseignants);
            // On récupère quelques admins déjà persistés
            List<Administration> adminsBulk = em.createQuery(
                            "SELECT a FROM Administration a", Administration.class)
                    .setMaxResults(10).getResultList();
            auteurs.addAll(adminsBulk);

            int nbHist = Math.max(10, scale / 5);
            for (int i = 0; i < nbHist; i++) {
                Utilisateur auteur = em.merge(pick(auteurs));
                Cours       co     = em.merge(pick(coursList));
                Salle       sal    = em.merge(pick(salles));
                Historique   h     = ModelFactory.historique(FAKER, auteur, co, sal, today);
                em.persist(h);
                auteur.getHistoriques().add(h);
                if (++count % BATCH_SIZE == 0) { em.flush(); em.clear(); }
            }

            // 14. Conflits
            int nbConflits = Math.max(5, scale / 20);
            for (int i = 0; i < nbConflits; i++) {
                Cours co  = em.merge(pick(coursList));
                Salle sal = em.merge(pick(salles));
                Conflit cf = ModelFactory.conflit(FAKER, co, sal, today);
                em.persist(cf);
                co.getConflits().add(cf);
                if (++count % BATCH_SIZE == 0) { em.flush(); em.clear(); }
            }

            // 15. Demandes de modification
            int nbDemandes = Math.max(5, scale / 15);
            for (int i = 0; i < nbDemandes; i++) {
                Enseignant ens = em.merge(pick(enseignants));
                Cours      co  = em.merge(pick(coursList));
                DemandeModification dm = ModelFactory.demandeModification(FAKER, ens, co, today);
                em.persist(dm);
                ens.getDemandes().add(dm);
                if (++count % BATCH_SIZE == 0) { em.flush(); em.clear(); }
            }
            */
            // modifier le printf afin qu'il soit comme ceci : System.out.printf("[DataInit] ✔ Bulk : %d étudiants | %d cours | %d absences | "
            //                            + "%d notifs | %d historiques | %d conflits | %d demandes insérés%n",
            //                    scale, nbCours, nbAbsences, nbNotifs, nbHist, nbConflits, nbDemandes);
            tx.commit();
            System.out.printf("[DataInit] ✔ Bulk : %d étudiants | %d cours | %d absences insérés%n",
                    scale, nbCours, nbAbsences);

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    private static <T> T pick(List<T> list)  { return list.get(RNG.nextInt(list.size())); }
    private static <T> T pick(T[] array)     { return array[RNG.nextInt(array.length)]; }


    //find : evite les doublons à chaque execution

>>>>>>> work

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