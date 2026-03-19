package fr.univ.hyperplanning.infra.config;

import net.datafaker.Faker;
import fr.univ.hyperplanning.infra.config.AuthService;
import fr.univ.hyperplanning.model.enums.*;
import fr.univ.hyperplanning.model.gestion.*;
import fr.univ.hyperplanning.model.pedagogie.*;
import fr.univ.hyperplanning.model.utilisateurs.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


public final class ModelFactory {

    private ModelFactory() {}

    // ── Salle ──────────────────────────────────────────────────────────────

    public static Salle salle(Faker f, int index) {
        Salle s = new Salle();
        s.setNumero("S" + String.format("%03d", index));
        s.setBatiment("Bat " + (char) ('A' + (index % 6)));
        s.setCapacite(f.number().numberBetween(15, 150));
        s.setTypeSalle(randomEnum(TypeSalle.class, f));
        s.setActif(true);
        return s;
    }

    // ── SyllabusSemestre ───────────────────────────────────────────────────

    public static SyllabusSemestre syllabus(NiveauGroupe niveau, int semestre) {
        SyllabusSemestre s = new SyllabusSemestre();
        s.setNiveau(niveau);
        s.setSemestre(semestre);
        s.setAnneeUniversitaire("2025-2026");
        if (semestre == 1) {
            s.setDateDebut(Date.valueOf(LocalDate.of(2025, 9, 1)));
            s.setDateFin(Date.valueOf(LocalDate.of(2026, 1, 31)));
        } else {
            s.setDateDebut(Date.valueOf(LocalDate.of(2026, 2, 1)));
            s.setDateFin(Date.valueOf(LocalDate.of(2026, 6, 30)));
        }
        return s;
    }

    // ── ModuleCours ────────────────────────────────────────────────────────

    public static ModuleCours module(Faker f, SyllabusSemestre syllabus, int index) {
        ModuleCours m = new ModuleCours();
        m.setCode("MOD" + String.format("%05d", index));
        m.setNom(f.educator().course());
        m.setNombreHeures(f.number().numberBetween(15, 60));
        m.setCoefficient(f.number().numberBetween(1, 5) * 0.5f);
        m.setSemestre(syllabus.getSemestre());
        m.setSyllabus(syllabus);
        return m;
    }

    // ── Groupe ─────────────────────────────────────────────────────────────

    public static Groupe groupe(SyllabusSemestre syllabus, NiveauGroupe niveau,
                                TypeGroupe type, int index) {
        Groupe g = new Groupe();
        g.setCode(niveau.name() + "_" + type.name() + "_" + String.format("%04d", index));
        g.setLibelle(niveau.name() + " " + type.name() + " " + index);
        g.setNiveau(niveau);
        g.setTypeGroupe(type);
        g.setEffectifMax(type == TypeGroupe.CM ? 150 : 30);
        g.setEffectifActuel(0);
        g.setAnneeUniversitaire(2025);
        g.setSyllabus(syllabus);
        return g;
    }

    // ── Enseignant ─────────────────────────────────────────────────────────

    public static Enseignant enseignant(Faker f, int index, Date today) {
        Enseignant e = new Enseignant();
        e.setNom(f.name().lastName());
        e.setPrenom(f.name().firstName());
        e.setEmail("ens_" + index + "_" + uid() + "@univ.fr");
        e.setMotDePasseHash(AuthService.hasher("ens1234"));
        e.setRole(RoleUtilisateur.ENSEIGNANT);
        e.setActif(true);
        e.setDateCreation(today);
        e.setDerniereConnexion(today);
        e.setNumeroEmploye("EMP" + String.format("%06d", index));
        e.setSpecialite(f.educator().course());
        e.setEstResponsable(false);
        return e;
    }

    // ── Etudiant ───────────────────────────────────────────────────────────

    public static Etudiant etudiant(Faker f, int index, Date today) {
        Etudiant e = new Etudiant();
        e.setNom(f.name().lastName());
        e.setPrenom(f.name().firstName());
        e.setEmail("etu_" + index + "_" + uid() + "@etu.univ.fr");
        e.setMotDePasseHash(AuthService.hasher("etu1234"));
        e.setRole(RoleUtilisateur.ETUDIANT);
        e.setActif(true);
        e.setDateCreation(today);
        e.setDerniereConnexion(today);
        e.setNumeroEtudiant("ETU" + String.format("%08d", index));
        e.setAnneeEntree(2023 + f.number().numberBetween(0, 3));
        return e;
    }

    // ── Intervenant ────────────────────────────────────────────────────────

    public static Intervenant intervenant(Faker f, int index, Date today) {
        Intervenant i = new Intervenant();
        i.setNom(f.name().lastName());
        i.setPrenom(f.name().firstName());
        i.setEmail("int_" + index + "_" + uid() + "@ext.fr");
        i.setMotDePasseHash(AuthService.hasher("int1234"));
        i.setRole(RoleUtilisateur.INTERVENANT);
        i.setActif(true);
        i.setDateCreation(today);
        i.setDerniereConnexion(today);
        i.setEntreprise(f.company().name());
        i.setContrat(f.options().option("Vacataire", "CDI", "CDD", "Freelance"));
        return i;
    }

    // ── Administration ─────────────────────────────────────────────────────

    public static Administration administration(Faker f, int index, Date today) {
        Administration a = new Administration();
        a.setNom(f.name().lastName());
        a.setPrenom(f.name().firstName());
        a.setEmail("admin_" + index + "_" + uid() + "@univ.fr");
        a.setMotDePasseHash(AuthService.hasher("admin1234"));
        a.setRole(RoleUtilisateur.ADMINISTRATION);
        a.setActif(true);
        a.setDateCreation(today);
        a.setDerniereConnexion(today);
        a.setService(f.options().option("Scolarité", "Direction", "RH", "Finances", "Communication"));
        a.setNiveauAcces(f.number().numberBetween(1, 4));
        return a;
    }

    // ── DonneePersonnelle ──────────────────────────────────────────────────

    public static DonneePersonnelle donneePersonnelle(Faker f, Utilisateur u) {
        DonneePersonnelle dp = new DonneePersonnelle();
        dp.setAdresse(f.address().fullAddress());
        dp.setTelephone(f.numerify("06########"));
        dp.setDateNaissance(Date.valueOf(LocalDate.of(
                f.number().numberBetween(1970, 2005),
                f.number().numberBetween(1, 12),
                f.number().numberBetween(1, 28))));
        dp.setLieuNaissance(f.address().city());
        dp.setPhotoUrl("https://i.pravatar.cc/150?u=" + u.getEmail());
        dp.setUtilisateur(u);
        u.setDonneePersonnelle(dp);
        return dp;
    }

    // ── Cours ──────────────────────────────────────────────────────────────

    public static Cours cours(Faker f, ModuleCours module, Salle salle, TypeCours type) {
        LocalDate date = LocalDate.of(2025, 9, 1)
                .plusDays(f.number().numberBetween(0, 180));
        Cours c = new Cours();
        c.setIntitule(module.getNom() + " – " + type.name());
        c.setTypeCours(type);
        c.setDateDebut(Date.valueOf(date));
        c.setDateFin(Date.valueOf(date));
        c.setDuree(type == TypeCours.CM ? 90 : 120);
        c.setEstCC(type == TypeCours.CC);
        c.setEstAnnule(false);
        c.setAnnotation(f.lorem().sentence());
        c.setModule(module);
        c.setSalle(salle);
        return c;
    }

    // ── Absence ────────────────────────────────────────────────────────────

    public static Absence absence(Faker f, Cours cours, Etudiant etudiant) {
        Absence a = new Absence();
        a.setDateAbsence(cours.getDateDebut());
        a.setMotif(f.options().option("Maladie", "Raison familiale", "Transport", "Problème personnel"));
        a.setJustificatif(null);
        a.setStatut(randomEnum(StatutAbsence.class, f));
        a.setDateDeclaration(LocalDateTime.now().minusDays(f.number().numberBetween(0, 10)));
        a.setDateValidation(null);
        a.setMotifRefus(null);
        a.setCours(cours);
        a.setEtudiantAbsent(etudiant);
        a.setDeclareePar(etudiant);
        a.setValideePar(null);
        return a;
    }

    // ── Utilitaires ────────────────────────────────────────────────────────

    public static <T extends Enum<T>> T randomEnum(Class<T> clazz, Faker f) {
        T[] values = clazz.getEnumConstants();
        return values[f.number().numberBetween(0, values.length)];
    }

    private static String uid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }
}
