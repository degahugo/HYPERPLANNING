package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import fr.univ.hyperplanning.app.SessionUtilisateur;
import fr.univ.hyperplanning.dao.CoursDAO;
import fr.univ.hyperplanning.model.pedagogie.Cours;
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

    private final CoursDAO coursDAO = new CoursDAO();

    @FXML
    public void initialize() {
        if (totalCoursLabel == null) {
            System.err.println("Alerte : totalCoursLabel est NULL. Vérifiez le fx:id dans le FXML.");
            return;
        }

        LocalDate now = LocalDate.now();
        Utilisateur user = SessionUtilisateur.get();

        /*List<Cours> tous = coursDAO.findAll();
        List<Cours> mois = coursDAO.findByMonth(now);
        List<Cours> jour = coursDAO.findByDay(now);
        */

        // Filtrer par l'étudiant connecté (via ses groupes)
        List<Cours> tous = coursDAO.findAllByEtudiant(user.getId());
        List<Cours> mois = coursDAO.findByEtudiant(user.getId(),
                now.withDayOfMonth(1),
                now.withDayOfMonth(now.lengthOfMonth()));
        List<Cours> jour = coursDAO.findByEtudiant(user.getId(), now, now);

        totalCoursLabel.setText(String.valueOf(tous.size()));
        coursMoisLabel.setText(String.valueOf(mois.size()));
        coursJourLabel.setText(String.valueOf(jour.size()));

        todayLessonsContainer.getChildren().clear();
        for (Cours c : jour) {
            todayLessonsContainer.getChildren().add(creerCardCoursFigma(c));
        }
    }

    private VBox creerCardCoursFigma(Cours c) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        // STYLE FIGMA : Blanc, ombre portée légère, bordure fine
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4); " +
                "-fx-border-color: #F3F4F6; -fx-border-radius: 16;");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4, Color.web("#2563EB")); // Point bleu Figma

        VBox textPart = new VBox(2);
        Label title = new Label(c.getIntitule());
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label subtitle = new Label((c.getModule() != null ? c.getModule().getCode() : "COURS") + " • 6 ECTS");
        subtitle.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12;");
        textPart.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(dot, textPart);

        ProgressBar pb = new ProgressBar(0.7);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setPrefHeight(6);
        pb.setStyle("-fx-accent: #111827; -fx-control-inner-background: #F3F4F6;");

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        Label prof = new Label("Dr. Rousseau"); // À dynamiser plus tard
        prof.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12;");
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        Label time = new Label(c.getDateDebut().toLocalTime().toString());
        time.setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold;");

        footer.getChildren().addAll(prof, s, time);
        card.getChildren().addAll(header, pb, footer);
        return card;
    }

    @FXML private void handleVoirTout() {
        MainController.getInstance().gererNavigation("PLANNING");
    }
}