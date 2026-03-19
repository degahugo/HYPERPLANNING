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


            Date today = Date.valueOf(LocalDate.now());

            Administration admin = findOrCreateAdmin(em, "admin@univ.fr", today);
            Enseignant ens   = findOrCreateEnseignant(em, "ens1@univ.fr", today);
            Enseignant ens2 = findOrCreateEnseignant(em, "ens2@univ.fr", today);
            Intervenant inter = findOrCreateIntervenant(em, "int1@univ.fr", today);
            Etudiant etu   = findOrCreateEtudiant(em, "etu1@univ.fr", today);

            // Données personnelles (une seule fois chacune)
            if (admin.getDonneePersonnelle() == null) em.persist(buildDP(admin));
            if (ens.getDonneePersonnelle()   == null) em.persist(buildDP(ens));
            if (inter.getDonneePersonnelle() == null) em.persist(buildDP(inter));
            if (etu.getDonneePersonnelle()   == null) em.persist(buildDP(etu));

            // Relations groupe <-> étudiant
            if (!etu.getGroupes().contains(g)) {
                etu.getGroupes().add(g);
                g.getEtudiants().add(etu);
            }


            LocalDate coursDate = LocalDate.now().plusDays(1);

            Cours cours = new Cours();
            cours.setIntitule("Algorithmique - TD");
            cours.setTypeCours(TypeCours.TD);
            cours.setDateDebut(Date.valueOf(coursDate));
            cours.setDateFin(Date.valueOf(coursDate));
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
            cours2.setDateDebut(Date.valueOf(coursDate2));
            cours2.setDateFin(Date.valueOf(coursDate2));
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
            cours3.setDateDebut(Date.valueOf(coursDate3));
            cours3.setDateFin(Date.valueOf(coursDate3));
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