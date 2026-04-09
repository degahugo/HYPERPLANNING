package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.app.SessionUtilisateur;
import fr.univ.hyperplanning.dao.CoursDAO;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.utilisateurs.Enseignant;
import fr.univ.hyperplanning.model.utilisateurs.Intervenant;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import fr.univ.hyperplanning.dao.AbsenceDAO;
import fr.univ.hyperplanning.model.gestion.Absence;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.time.LocalDate;
import java.util.List;

public class DashboardEtudiantController {

    @FXML private Label totalCoursLabel;
    @FXML private Label coursMoisLabel;
    @FXML private Label coursJourLabel;
    @FXML private VBox todayLessonsContainer;
    @FXML private VBox cardMesAbsences;
    @FXML private Label mesAbsencesCountLabel;
    private final AbsenceDAO absenceDAO = new AbsenceDAO();

    private final CoursDAO coursDAO = new CoursDAO();

    @FXML
    public void initialize() {
        Utilisateur user = SessionUtilisateur.get();


        if (user == null || totalCoursLabel == null) {
            System.err.println("Erreur : Session utilisateur ou composants FXML manquants.");
            return;
        }

        LocalDate now = LocalDate.now();
        LocalDate debutSemestre = LocalDate.of(2026, 1, 1);
        LocalDate finSemestre = LocalDate.of(2026, 7, 1);

        List<Cours> tous = coursDAO.findByEtudiant(user.getId(), debutSemestre, finSemestre);
        List<Cours> mois = coursDAO.findByEtudiant(user.getId(),
                now.withDayOfMonth(1), now.withDayOfMonth(now.lengthOfMonth()));
        List<Cours> jour = coursDAO.findByEtudiant(user.getId(), now, now);

        totalCoursLabel.setText(String.valueOf(tous.size()));
        coursMoisLabel.setText(String.valueOf(mois.size()));
        coursJourLabel.setText(String.valueOf(jour.size()));

        // AFFICHAGE DES CARTES DU JOUR
        todayLessonsContainer.getChildren().clear();
        if (jour.isEmpty()) {
            Label vide = new Label("Aucun cours aujourd'hui ✨");
            vide.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14; -fx-font-style: italic;");
            todayLessonsContainer.getChildren().add(vide);
        } else {
            for (Cours c : jour) {
                todayLessonsContainer.getChildren().add(creerCardCoursFigma(c));
            }
        }

        initialiserCarteAbsences(user);
    }

    private void initialiserCarteAbsences(Utilisateur user) {
        if (user == null) return;

        if (mesAbsencesCountLabel != null) {
            mesAbsencesCountLabel.setText(String.valueOf(
                    absenceDAO.countAbsencesByUtilisateur(user.getId())
            ));
        }

        if (cardMesAbsences != null) {
            cardMesAbsences.setOnMouseClicked(e -> {
                if (MainController.getInstance() != null) {
                    MainController.getInstance().gererNavigation("MES_ABSENCES");
                }
            });
        }
    }

    /**
     * Gère l'affichage de tous les intervenants sur plusieurs lignes
     */
    private String obtenirNomProf(Cours c) {
        StringBuilder sb = new StringBuilder();
        if (c.getEnseignantsEnseignes() != null) {
            for (Enseignant e : c.getEnseignantsEnseignes()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("👤 ").append(e.getNom().toUpperCase()).append(" ").append(e.getPrenom().substring(0, 1)).append(".");
            }
        }
        if (c.getIntervenantsInterventions() != null) {
            for (Intervenant i : c.getIntervenantsInterventions()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("👤 ").append(i.getNom().toUpperCase()).append(" (Ext.)");
            }
        }
        return sb.length() > 0 ? sb.toString() : "👤 Non assigné";
    }

    private VBox creerCardCoursFigma(Cours c) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4); " +
                "-fx-border-color: #F3F4F6; -fx-border-radius: 16;");

        // HEADER
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4, Color.web("#2563EB"));

        VBox textPart = new VBox(2);
        Label title = new Label(c.getIntitule());
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #111827;");

        String type = (c.getTypeCours() != null) ? c.getTypeCours().name() : "COURS";
        Label subtitle = new Label((c.getModule() != null ? c.getModule().getCode() : "UE") + " • " + type);
        subtitle.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12;");

        textPart.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(dot, textPart);

        ProgressBar pb = new ProgressBar(0.7);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setPrefHeight(6);
        pb.setStyle("-fx-accent: #111827; -fx-control-inner-background: #F3F4F6;");

        HBox footer = new HBox();
        footer.setAlignment(Pos.BOTTOM_LEFT);

        VBox infoBox = new VBox(5);
        Label profLabel = new Label(obtenirNomProf(c));
        profLabel.setWrapText(true);
        profLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11;");

        Label salleLabel = new Label("📍 Salle " + (c.getSalle() != null ? c.getSalle().getNumero() : "N/A"));
        salleLabel.setStyle("-fx-text-fill: #111827; -fx-font-size: 11; -fx-font-weight: bold;");

        infoBox.getChildren().addAll(profLabel, salleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label time = new Label(c.getDateDebut().toLocalTime().toString());
        time.setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-font-size: 13;");

        footer.getChildren().addAll(infoBox, spacer, time);
        card.getChildren().addAll(header, pb, footer);

        return card;
    }

    @FXML private void handleVoirTout() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().gererNavigation("PLANNING");
        }
    }
}