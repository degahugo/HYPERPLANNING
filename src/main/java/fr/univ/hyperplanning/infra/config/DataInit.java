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

import java.util.HashMap;
import java.util.Map;

public class DataInit {

    private static final Faker   FAKER = new Faker(Locale.FRENCH);
    private static final Random  RNG   = new Random();

    /*
    seed() données de référence fixes, appelé depuis init() de MainApp
    Rapide, peut être bloquant mais c'est acceptable avent l'affichage
    */

    public static void seed() {

        //  Guard global : si le groupe "L2TD1" existe déjà, tout a déjà été initialisé
        EntityManager check = JpaUtil.em();
        try {
            Long count = check.createQuery(
                            "SELECT COUNT(g) FROM Groupe g WHERE g.code = :code", Long.class)
                    .setParameter("code", "L2TD1")
                    .getSingleResult();
            if (count > 0) {
                System.out.println("[DataInit] Données déjà présentes, seed ignoré.");
                return;
            }
        } finally {
            check.close();
        }

        EntityManager em = JpaUtil.em();
        var tx = em.getTransaction();

        try {
            tx.begin();

            Salle salle = findOrCreateSalle(em, "S101", "Bat A");
            Salle salle2 = findOrCreateSalle(em, "S102", "Bat B");



            SyllabusSemestre syllabus = findOrCreateSyllabus(em, "2025-2026", 1, NiveauGroupe.L2);


            ModuleCours mAlgo   = findOrCreateModule(em, "INFO201", "Algorithmique",           30, 2.0f, 1, syllabus);
            ModuleCours mProjet = findOrCreateModule(em, "INFO202", "Méthodologie de projet",  20, 1.5f, 1, syllabus);
            ModuleCours mBDD    = findOrCreateModule(em, "INFO203", "Base de données", 30, 2.0f, 1, syllabus);


            Groupe g = new Groupe();
            g.setCode("L2TD1");
            g.setLibelle("L2 TD1");
            g.setNiveau(NiveauGroupe.L2);
            g.setTypeGroupe(TypeGroupe.TD);
            g.setEffectifMax(30);
            g.setEffectifActuel(1);
            g.setAnneeUniversitaire(2025);
            g.setSyllabus(syllabus);
            em.persist(g);

            // AJOUT SPRINT 4 : Groupe 2 pour tester le filtrage promo
            Groupe g2 = new Groupe();
            g2.setCode("L2TD2");
            g2.setLibelle("L2 TD2");
            g2.setNiveau(NiveauGroupe.L2);
            g2.setTypeGroupe(TypeGroupe.TD);
            g2.setEffectifMax(30);
            g2.setEffectifActuel(0);
            g2.setAnneeUniversitaire(2025);
            g2.setSyllabus(syllabus);
            em.persist(g2);


            Date today = Date.valueOf(LocalDate.now());

            Administration admin = findOrCreateAdmin(em, "admin@univ.fr", today);
            Enseignant ens   = findOrCreateEnseignant(em, "ens1@univ.fr", today);
            Enseignant ens2 = findOrCreateEnseignant(em, "ens2@univ.fr", today);
            Intervenant inter = findOrCreateIntervenant(em, "int1@univ.fr", today);

            // Étudiant 1
            Etudiant etu   = findOrCreateEtudiant(em, "etu1@univ.fr", today);

            // --- ÉTUDIANT 2 : Création manuelle pour éviter le conflit de matricule ---
            Etudiant etu2;
            try {
                // On vérifie s'il existe déjà pour ne pas créer de doublon d'email
                etu2 = em.createQuery("SELECT e FROM Etudiant e WHERE e.email = :e", Etudiant.class)
                        .setParameter("e", "etu2@univ.fr").getSingleResult();
            } catch (NoResultException e) {
                // S'il n'existe pas, on le crée de A à Z proprement
                etu2 = new Etudiant();
                etu2.setNom("BENOIT");
                etu2.setPrenom("Marc");
                etu2.setEmail("etu2@univ.fr");
                etu2.setMotDePasseHash(AuthService.hasher("etu2345"));
                etu2.setRole(RoleUtilisateur.ETUDIANT);
                                                            etu2.setAnneeEntree(2025);
                                                            etu2.setActif(true);
                                                            etu2.setDerniereConnexion(today);

                // ICI : Tu mets le bon numéro DIRECTEMENT
                etu2.setNumeroEtudiant("ETU20250002");
                etu2.setDateCreation(today);

                em.persist(etu2); // On le persiste une seule fois avec les bonnes infos
            }

            // Données personnelles (une seule fois chacune)
            if (admin.getDonneePersonnelle() == null) em.persist(buildDP(admin));
            if (ens.getDonneePersonnelle()   == null) em.persist(buildDP(ens));
            if (inter.getDonneePersonnelle() == null) em.persist(buildDP(inter));
            if (etu.getDonneePersonnelle()   == null) em.persist(buildDP(etu));
            if (etu2.getDonneePersonnelle()  == null) em.persist(buildDP(etu2));

            // Relations groupe <-> étudiant 1
            if (!etu.getGroupes().contains(g)) {
                etu.getGroupes().add(g);
                g.getEtudiants().add(etu);
            }

            // Relations groupe <-> étudiant 2
            if (!etu2.getGroupes().contains(g2)) {
                etu2.getGroupes().add(g2);
                g2.getEtudiants().add(etu2);
            }


            LocalDate coursDate = LocalDate.now().plusDays(1);

            // MODIFICATION SPRINT 4 : CM pour la PROMO (G1 + G2)
            Cours cours = new Cours();
            cours.setIntitule("Algorithmique - CM (PROMO)");
            cours.setTypeCours(TypeCours.CM);
            cours.setDateDebut(coursDate.atTime(8, 0));
            cours.setDateFin(coursDate.atTime(10, 0));
            cours.setDuree(120);
            cours.setEstCC(false);
            cours.setEstAnnule(false);
            cours.setAnnotation("Cours magistral");
            cours.setSalle(salle);
            cours.setModule(mAlgo);

            cours.getGroupes().add(g);
            cours.getGroupes().add(g2); // Liaison aux deux groupes
            g.getCours().add(cours);
            g2.getCours().add(cours);
            ens.getCoursEnseignes().add(cours);
            inter.getCoursInterventions().add(cours);

            em.persist(cours);

            // MODIFICATION SPRINT 4 : TD spécifique au GROUPE 1
            LocalDate coursDate2 = LocalDate.now().plusDays(2);

            Cours cours2 = new Cours();
            cours2.setIntitule("Algorithmique - TD G1");
            cours2.setTypeCours(TypeCours.TD);
            cours2.setDateDebut(coursDate2.atTime(9, 0));
            cours2.setDateFin(coursDate2.atTime(11, 0));
            cours2.setDuree(120);
            cours2.setEstCC(false);
            cours2.setEstAnnule(false);
            cours2.setAnnotation("Séance TD Groupe 1");
            cours2.setSalle(salle2);
            cours2.setModule(mAlgo);

            cours2.getGroupes().add(g);
            g.getCours().add(cours2);

            ens.getCoursEnseignes().add(cours2);

            em.persist(cours2);

            // MODIFICATION SPRINT 4 : TD spécifique au GROUPE 2 (Invisible pour etu1 si filtré)
            LocalDate coursDate3 = LocalDate.now().plusDays(3);

            Cours cours3 = new Cours();
            cours3.setIntitule("Algorithmique - TD G2");
            cours3.setTypeCours(TypeCours.TD);
            cours3.setDateDebut(coursDate3.atTime(14, 0));
            cours3.setDateFin(coursDate3.atTime(16, 0));
            cours3.setDuree(120);
            cours3.setEstCC(false);
            cours3.setEstAnnule(false);
            cours3.setAnnotation("Séance TD Groupe 2");
            cours3.setSalle(salle2);
            cours3.setModule(mAlgo);

            cours3.getGroupes().add(g2);
            g2.getCours().add(cours3);

            ens.getCoursEnseignes().add(cours3);

            em.persist(cours3);

            // --- NOUVEAU JEU DE DONNÉE : Cours exclusif Enseignant ---
            // --- COURS EXCLUSIF ENSEIGNANT ---
            Cours coursExpert = new Cours();
            coursExpert.setIntitule("Expertise Java - Avancé");
            coursExpert.setTypeCours(TypeCours.TD);
            coursExpert.setDateDebut(coursDate.plusDays(3).atTime(10, 0));
            coursExpert.setDateFin(coursDate.plusDays(3).atTime(12, 0));
            coursExpert.setDuree(120);            // AJOUTÉ
            coursExpert.setEstCC(false);          // AJOUTÉ
            coursExpert.setEstAnnule(false);      // AJOUTÉ
            coursExpert.setAnnotation("Cours expert"); // AJOUTÉ (L'ERREUR VENAIT DE LÀ)
            coursExpert.setSalle(salle);
            coursExpert.setModule(mProjet);
            coursExpert.getGroupes().add(g);
            coursExpert.getGroupes().add(g2);
            ens.getCoursEnseignes().add(coursExpert);
            em.persist(coursExpert);

// --- NOUVEAU JEU DE DONNÉE : Cours exclusif Intervenant ---
            Cours coursConf = new Cours();
            coursConf.setIntitule("Conférence Métier");
            coursConf.setTypeCours(TypeCours.CM);
            coursConf.setDateDebut(coursDate.plusDays(4).atTime(14, 0));
            coursConf.setDateFin(coursDate.plusDays(4).atTime(16, 0));
            coursConf.setDuree(120);            // AJOUTÉ
            coursConf.setEstCC(false);          // AJOUTÉ
            coursConf.setEstAnnule(false);      // AJOUTÉ
            coursConf.setAnnotation("Conférence externe"); // AJOUTÉ
            coursConf.setSalle(salle2);
            coursConf.setModule(mProjet);
            coursConf.getGroupes().add(g);
            coursConf.getGroupes().add(g2);
            inter.getCoursInterventions().add(coursConf);
            em.persist(coursConf);

// --- COURS DE TEST CONFLIT : même salle, même créneau ---
            Cours coursConflitA = new Cours();
            coursConflitA.setIntitule("BDD - Conflit Test A");
            coursConflitA.setTypeCours(TypeCours.TD);
            coursConflitA.setDateDebut(LocalDate.now().plusDays(1).atTime(8, 0));  // même que cours
            coursConflitA.setDateFin(LocalDate.now().plusDays(1).atTime(10, 0));   // même que cours
            coursConflitA.setDuree(120);
            coursConflitA.setEstCC(false);
            coursConflitA.setEstAnnule(false);
            coursConflitA.setAnnotation("Test conflit");
            coursConflitA.setSalle(salle);     // MÊME salle que cours (S101) → conflit de salle
            coursConflitA.setModule(mBDD);
            coursConflitA.getGroupes().add(g);
            em.persist(coursConflitA);



            Absence absence = new Absence();
            absence.setDateAbsence(Date.valueOf(coursDate));
            absence.setMotif("Maladie");
            absence.setPiecesJointesPaths(new java.util.ArrayList<>());
            absence.setStatut(StatutAbsence.DECLAREE);
            absence.setDateDeclaration(LocalDateTime.now());
            absence.setDateValidation(null);
            absence.setMotifRefus(null);
            absence.setCours(cours);
            absence.setUtilisateurAbsent(etu);
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


            //DemandeModification

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

            tx.commit();
            System.out.println("[DataInit] Seed terminé avec succès.");

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

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

            // 4bis. Associer un sous-ensemble de modules à chaque groupe
            Map<Long, List<ModuleCours>> modulesParGroupe = new HashMap<>();
            for (Groupe grp : groupes) {
                Groupe grpManaged = em.merge(grp);

                List<ModuleCours> modulesCompatibles = modules.stream()
                        .filter(m -> m.getSyllabus() != null
                                && grpManaged.getSyllabus() != null
                                && m.getSyllabus().getId().equals(grpManaged.getSyllabus().getId()))
                        .toList();

                List<ModuleCours> selection = new ArrayList<>();

                int nbModulesPourGroupe = Math.min(4, modulesCompatibles.size());

                List<ModuleCours> copie = new ArrayList<>(modulesCompatibles);

                for (int i = 0; i < nbModulesPourGroupe; i++) {
                    ModuleCours choisi = pick(copie);
                    selection.add(choisi);
                    copie.remove(choisi);
                }

                modulesParGroupe.put(grpManaged.getId(), selection);
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
            List<Intervenant> intervenants = new ArrayList<>(); // ← liste manquante
            for (int i = 1; i <= Math.max(3, scale / 20); i++) {
                Intervenant inter = ModelFactory.intervenant(FAKER, i, today);
                em.persist(inter);
                em.persist(ModelFactory.donneePersonnelle(FAKER, inter));
                intervenants.add(inter); // ← jamais stocké avant
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
            /*List<Cours> coursList = new ArrayList<>();
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
            }*/

            // 9. Cours
            List<Cours> coursList = new ArrayList<>();
            int nbCours = Math.max(20, scale / 2);

            for (int i = 0; i < nbCours; i++) {
                Groupe grp = em.merge(pick(groupes));

                List<ModuleCours> modulesDuGroupe = modulesParGroupe.get(grp.getId());
                if (modulesDuGroupe == null || modulesDuGroupe.isEmpty()) {
                    continue;
                }

                ModuleCours mod = em.merge(pick(modulesDuGroupe));
                Salle sal = em.merge(pick(salles));
                TypeCours type = pick(TypeCours.values());

                Cours c = ModelFactory.cours(FAKER, mod, sal, type);

                c.getGroupes().add(grp);
                grp.getCours().add(c);

                Enseignant ens = em.merge(pick(enseignants));
                ens.getCoursEnseignes().add(c);

                //Assigner un intervant sur certains cours
                if (!intervenants.isEmpty() && RNG.nextInt(10) < 3) {
                    Intervenant inv = em.merge(pick(intervenants));
                    inv.getCoursInterventions().add(c);
                }

                em.persist(c);
                coursList.add(c);

                if (++count % BATCH_SIZE == 0) {
                    em.flush();
                    em.clear();
                }
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

    private static SyllabusSemestre findOrCreateSyllabus(EntityManager em, String annee,
                                                         int semestre, NiveauGroupe niveau) {
        try {
            return em.createQuery(
                            "SELECT s FROM SyllabusSemestre s " +
                                    "WHERE s.anneeUniversitaire = :a AND s.semestre = :s AND s.niveau = :n",
                            SyllabusSemestre.class)
                    .setParameter("a", annee)
                    .setParameter("s", semestre)
                    .setParameter("n", niveau)
                    .getSingleResult();
        } catch (NoResultException e) {
            SyllabusSemestre s = new SyllabusSemestre();
            s.setAnneeUniversitaire(annee);
            s.setSemestre(semestre);
            s.setNiveau(niveau);
            s.setDateDebut(Date.valueOf(LocalDate.of(2025, 9, 1)));
            s.setDateFin(Date.valueOf(LocalDate.of(2026, 1, 31)));
            em.persist(s);
            return s;
        }
    }

    private static ModuleCours findOrCreateModule(EntityManager em, String code, String nom,
                                                  int heures, float coeff, int semestre,
                                                  SyllabusSemestre syllabus) {
        try {
            return em.createQuery("SELECT m FROM ModuleCours m WHERE m.code = :c", ModuleCours.class)
                    .setParameter("c", code).getSingleResult();
        } catch (NoResultException e) {
            ModuleCours m = new ModuleCours();
            m.setCode(code);
            m.setNom(nom);
            m.setNombreHeures(heures);
            m.setCoefficient(coeff);
            m.setSemestre(semestre);
            m.setSyllabus(syllabus);
            em.persist(m);
            return m;
        }
    }

    private static Administration findOrCreateAdmin(EntityManager em, String email, Date today) {
        try {
            return em.createQuery(
                            "SELECT a FROM Administration a WHERE a.email = :e", Administration.class)
                    .setParameter("e", email).getSingleResult();
        } catch (NoResultException e) {
            Administration a = new Administration();
            a.setNom("Admin");
            a.setPrenom("System");
            a.setEmail(email);
            a.setMotDePasseHash(AuthService.hasher("admin1234"));

            a.setRole(RoleUtilisateur.ADMINISTRATION);
            a.setActif(true);
            a.setDateCreation(today);
            a.setDerniereConnexion(today);
            a.setService("Direction");
            a.setNiveauAcces(3);
            em.persist(a);
            return a;
        }
    }

    private static Enseignant findOrCreateEnseignant(EntityManager em, String email, Date today) {
        try {
            return em.createQuery(
                            "SELECT e FROM Enseignant e WHERE e.email = :e", Enseignant.class)
                    .setParameter("e", email).getSingleResult();
        } catch (NoResultException e) {
            Enseignant ens = new Enseignant();
            ens.setNom("Dupont");
            ens.setPrenom("Jean");
            ens.setEmail(email);
            ens.setMotDePasseHash(AuthService.hasher("ens1234"));
            ens.setRole(RoleUtilisateur.ENSEIGNANT);
            ens.setActif(true);
            ens.setDateCreation(today);
            ens.setDerniereConnexion(today);
            ens.setNumeroEmploye("EMP001");
            ens.setSpecialite("Informatique");
            ens.setEstResponsable(false);
            em.persist(ens);
            return ens;
        }
    }

    private static Intervenant findOrCreateIntervenant(EntityManager em, String email, Date today) {
        try {
            return em.createQuery(
                            "SELECT i FROM Intervenant i WHERE i.email = :e", Intervenant.class)
                    .setParameter("e", email).getSingleResult();
        } catch (NoResultException e) {
            Intervenant i = new Intervenant();
            i.setNom("Martin");
            i.setPrenom("Sara");
            i.setEmail(email);
            i.setMotDePasseHash(AuthService.hasher("int1234"));
            i.setRole(RoleUtilisateur.INTERVENANT);
            i.setActif(true);
            i.setDateCreation(today);
            i.setDerniereConnexion(today);
            i.setEntreprise("TechCorp");
            i.setContrat("Vacataire");
            em.persist(i);
            return i;
        }
    }

    private static Etudiant findOrCreateEtudiant(EntityManager em, String email, Date today) {
        try {
            return em.createQuery(
                            "SELECT e FROM Etudiant e WHERE e.email = :e", Etudiant.class)
                    .setParameter("e", email).getSingleResult();
        } catch (NoResultException e) {
            Etudiant etu = new Etudiant();
            etu.setNom("Durand");
            etu.setPrenom("Alice");
            etu.setEmail(email);
            etu.setMotDePasseHash(AuthService.hasher("etu1234"));
            etu.setRole(RoleUtilisateur.ETUDIANT);
            etu.setActif(true);
            etu.setDateCreation(today);
            etu.setDerniereConnexion(today);
            etu.setNumeroEtudiant("ETU20250001");
            etu.setAnneeEntree(2025);
            em.persist(etu);
            return etu;
        }
    }

    private static DonneePersonnelle buildDP(Utilisateur u) {
        DonneePersonnelle dp = new DonneePersonnelle();
        dp.setAdresse("1 rue de l'Université, 75000 Paris");
        dp.setTelephone("0000000000");
        dp.setDateNaissance(Date.valueOf(LocalDate.of(1990, 1, 1)));
        dp.setLieuNaissance("Paris");
        dp.setPhotoUrl("https://placeholder.com/photo.jpg");
        dp.setUtilisateur(u);
        u.setDonneePersonnelle(dp);
        return dp;
    }
}