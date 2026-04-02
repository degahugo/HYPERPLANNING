package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.app.SessionUtilisateur;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.utilisateurs.*;
import jakarta.persistence.EntityManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

public class ProfilController {
    @FXML private VBox profilContainer;

    @FXML
    public void initialize() {
        Utilisateur session = SessionUtilisateur.get();
        if (session == null) return;

        EntityManager em = JpaUtil.em();
        try {
            Utilisateur user = em.find(session.getClass(), session.getId());
            if (user == null) user = em.find(Utilisateur.class, session.getId());
            construirePage(user);
        } finally {
            em.close();
        }
    }

    private void construirePage(Utilisateur user) {
        profilContainer.getChildren().clear();

        VBox header = new VBox(12);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30));
        header.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        StackPane avatar = new StackPane();
        Circle cercle = new Circle(40, Color.web(couleurRole(user)));
        Label initiales = new Label(
                user.getPrenom().substring(0, 1).toUpperCase() +
                        user.getNom().substring(0, 1).toUpperCase());
        initiales.setStyle("-fx-font-size: 24; -fx-font-weight: 900; -fx-text-fill: white;");
        avatar.getChildren().addAll(cercle, initiales);

        Label nomComplet = new Label(user.getPrenom() + " " + user.getNom());
        nomComplet.setStyle("-fx-font-size: 22; -fx-font-weight: 800; -fx-text-fill: #111827;");

        Label badgeRole = new Label(labelRole(user));
        badgeRole.setStyle("-fx-background-color: " + couleurBadge(user) + "; " +
                "-fx-text-fill: " + couleurRole(user) + "; " +
                "-fx-padding: 4 14; -fx-background-radius: 20; " +
                "-fx-font-size: 12; -fx-font-weight: bold;");

        Label emailLabel = new Label(user.getEmail());
        emailLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13;");

        header.getChildren().addAll(avatar, nomComplet, badgeRole, emailLabel);

        VBox sectionPerso = creerSection("Informations personnelles");
        DonneePersonnelle dp = user.getDonneePersonnelle();
        if (dp != null) {
            sectionPerso.getChildren().addAll(
                    creerLigne("Téléphone", dp.getTelephone()),
                    creerLigne("Adresse", dp.getAdresse()),
                    creerLigne("Date de naissance", dp.getDateNaissance() != null
                            ? dp.getDateNaissance().toString() : "—"),
                    creerLigne("Lieu de naissance", dp.getLieuNaissance())
            );
        } else {
            Label vide = new Label("Aucune donnée personnelle renseignée.");
            vide.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic;");
            sectionPerso.getChildren().add(vide);
        }

        VBox sectionRole = creerSection("Informations académiques");
        if (user instanceof Etudiant etu) {
            sectionRole.getChildren().addAll(
                    creerLigne("Numéro étudiant", etu.getNumeroEtudiant()),
                    creerLigne("Année d'entrée", String.valueOf(etu.getAnneeEntree())),
                    creerLigne("Groupes inscrits", etu.getGroupes().isEmpty()
                            ? "Aucun groupe"
                            : etu.getGroupes().stream()
                            .map(g -> g.getLibelle())
                            .reduce((a, b) -> a + ", " + b).orElse("—"))
            );
        } else if (user instanceof Enseignant ens) {
            sectionRole.getChildren().addAll(
                    creerLigne("Numéro employé", ens.getNumeroEmploye()),
                    creerLigne("Spécialité", ens.getSpecialite()),
                    creerLigne("Responsable", ens.isEstResponsable() ? "Oui" : "Non"),
                    creerLigne("Cours enseignés", String.valueOf(ens.getCoursEnseignes().size()))
            );
        } else if (user instanceof Intervenant inv) {
            sectionRole.getChildren().addAll(
                    creerLigne("Entreprise", inv.getEntreprise()),
                    creerLigne("Type de contrat", inv.getContrat()),
                    creerLigne("Interventions", String.valueOf(inv.getCoursInterventions().size()))
            );
        }

        VBox sectionCompte = creerSection("Compte");
        sectionCompte.getChildren().addAll(
                creerLigne("Membre depuis", user.getDateCreation() != null
                        ? user.getDateCreation().toString() : "—"),
                creerLigne("Dernière connexion", user.getDerniereConnexion() != null
                        ? user.getDerniereConnexion().toString() : "—"),
                creerLigne("Statut", user.isActif() ? "Actif" : "Inactif")
        );

        profilContainer.getChildren().addAll(header, sectionPerso, sectionRole, sectionCompte);
    }

    private VBox creerSection(String titre) {
        VBox section = new VBox(14);
        section.setPadding(new Insets(25));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        Label titreLabel = new Label(titre);
        titreLabel.setStyle("-fx-font-size: 15; -fx-font-weight: 800; -fx-text-fill: #111827;");
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #F3F4F6;");
        section.getChildren().addAll(titreLabel, sep);
        return section;
    }

    private HBox creerLigne(String cle, String valeur) {
        HBox ligne = new HBox();
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.setPadding(new Insets(6, 0, 6, 0));
        Label lblCle = new Label(cle);
        lblCle.setMinWidth(200);
        lblCle.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13;");
        Label lblVal = new Label(valeur != null ? valeur : "—");
        lblVal.setStyle("-fx-text-fill: #111827; -fx-font-size: 13; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        ligne.getChildren().addAll(lblCle, spacer, lblVal);
        return ligne;
    }

    private String couleurRole(Utilisateur user) {
        if (user instanceof Etudiant) return "#3B82F6";
        if (user instanceof Enseignant) return "#8B5CF6";
        if (user instanceof Intervenant) return "#F59E0B";
        return "#10B981";
    }

    private String couleurBadge(Utilisateur user) {
        if (user instanceof Etudiant) return "#EFF6FF";
        if (user instanceof Enseignant) return "#F5F3FF";
        if (user instanceof Intervenant) return "#FFFBEB";
        return "#ECFDF5";
    }

    private String labelRole(Utilisateur user) {
        if (user instanceof Etudiant) return "Étudiant";
        if (user instanceof Enseignant) return "Enseignant";
        if (user instanceof Intervenant) return "Intervenant";
        return "Administration";
    }
}