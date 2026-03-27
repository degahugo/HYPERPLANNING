package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.app.SessionUtilisateur;
import fr.univ.hyperplanning.dao.CoursDAO;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.utilisateurs.Enseignant;
import fr.univ.hyperplanning.model.utilisateurs.Etudiant;
import fr.univ.hyperplanning.model.utilisateurs.Intervenant;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PlanningController {

    private final CoursDAO coursDAO = new CoursDAO();

    @FXML private Label weekLabel;
    @FXML private HBox planningContainer;
    @FXML private Button btnWeek;
    @FXML private Button btnDay;

    private LocalDate currentDate;
    private boolean isWeekView = true;

    @FXML
    public void initialize() {
        currentDate = LocalDate.now();
        updateToggleButtonStyles();
        chargerDonnees();
    }

    private void mettreAJourTitre(LocalDate date) {
        LocalDate debut = date.with(DayOfWeek.MONDAY);
        LocalDate fin = debut.plusDays(6);

        String dateTexte = debut.getDayOfMonth() + " " +
                debut.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH) +
                " - " +
                fin.getDayOfMonth() + " " +
                fin.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH) +
                " " + fin.getYear();

        weekLabel.setText(dateTexte);
    }

    private void chargerDonnees() {
        mettreAJourTitre(currentDate);
        if (isWeekView) {
            chargerSemaine(currentDate);
        } else {
            chargerVueJourSemaine(currentDate);
        }
    }

    private List<Cours> recupererCoursFiltres(LocalDate debut, LocalDate fin) {
        Utilisateur user = SessionUtilisateur.get();
        if (user == null) return List.of();

        if (user instanceof Etudiant) {
            // Mission D : Filtrage par les groupes de l'étudiant
            return coursDAO.findByEtudiant(user.getId(), debut, fin);
        } else if (user instanceof Enseignant) {
            // Mission C : Filtrage par les cours assignés à l'enseignant
            return coursDAO.findByEnseignant(user.getId(), debut, fin);
        } else if (user instanceof Intervenant) {
            // Mission C : Filtrage par les cours assignés à l'intervenant
            return coursDAO.findByIntervenant(user.getId(), debut, fin);
        } else {
            // Admin ou autre : Vue globale sur la période
            return coursDAO.findByDateRange(debut, fin);
        }
    }

    private String obtenirNomProf(Cours c) {
        if (!c.getEnseignantsEnseignes().isEmpty()) {
            Enseignant e = c.getEnseignantsEnseignes().iterator().next();
            return e.getNom().toUpperCase() + " " + e.getPrenom().substring(0, 1) + ".";
        } else if (!c.getIntervenantsInterventions().isEmpty()) {
            Intervenant i = c.getIntervenantsInterventions().iterator().next();
            return i.getNom().toUpperCase() + " (Ext.)";
        }
        return "Non assigné";
    }

    private void chargerSemaine(LocalDate date) {
        LocalDate debut = date.with(DayOfWeek.MONDAY);
        LocalDate fin = debut.plusDays(6);

        List<Cours> cours = recupererCoursFiltres(debut, fin);

        planningContainer.getChildren().clear();
        VBox mainGrid = new VBox(0);
        HBox header = new HBox(0);

        Label spacerHeure = new Label("Heure");
        spacerHeure.setMinWidth(70);
        spacerHeure.setStyle("-fx-padding: 10; -fx-text-fill: #9CA3AF; -fx-font-size: 13;");
        header.getChildren().add(spacerHeure);

        double colWidth = 140;

        for (int i = 0; i < 7; i++) {
            LocalDate jour = debut.plusDays(i);
            VBox dayHeader = new VBox(2);
            dayHeader.setAlignment(Pos.CENTER);
            dayHeader.setMinWidth(colWidth);
            dayHeader.setPadding(new Insets(10));

            boolean isToday = jour.equals(LocalDate.now());
            Label name = new Label(capitalize(jour.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH)));
            Label num = new Label(jour.getDayOfMonth() + "/" + jour.getMonthValue());

            name.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (isToday ? "#3B82F6" : "#4B5563") + "; -fx-font-size: 14;");
            num.setStyle("-fx-text-fill: " + (isToday ? "#3B82F6" : "#9CA3AF") + "; -fx-font-size: 12;");

            if(isToday) dayHeader.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 12 12 0 0;");
            dayHeader.getChildren().addAll(name, num);
            header.getChildren().add(dayHeader);
        }
        mainGrid.getChildren().add(header);

        HBox body = new HBox(0);
        VBox timeCol = new VBox(0);
        for (int h = 8; h <= 18; h++) {
            Label hl = new Label(String.format("%02d:00", h));
            hl.setMinHeight(80); hl.setMinWidth(70);
            hl.setAlignment(Pos.TOP_CENTER);
            hl.setStyle("-fx-text-fill: #9CA3AF; -fx-padding: 5 0 0 0;");
            timeCol.getChildren().add(hl);
        }
        body.getChildren().add(timeCol);

        for (int i = 0; i < 7; i++) {
            LocalDate jour = debut.plusDays(i);
            Pane dayPane = new Pane();
            dayPane.setMinWidth(colWidth);
            if (jour.equals(LocalDate.now())) dayPane.setStyle("-fx-background-color: #F8FAFC;");

            for (int h = 0; h <= 10; h++) {
                Line line = new Line(0, h * 80, colWidth, h * 80);
                line.setStroke(Color.web("#F1F5F9"));
                dayPane.getChildren().add(line);
            }

            for (Cours c : cours) {
                if (c.getDateDebut().toLocalDate().equals(jour)) {
                    double startY = ((c.getDateDebut().getHour() - 8) * 80) + (c.getDateDebut().getMinute() * 80.0 / 60.0);
                    double hCell = ((c.getDateFin().getHour() - c.getDateDebut().getHour()) * 80) +
                            ((c.getDateFin().getMinute() - c.getDateDebut().getMinute()) * 80.0 / 60.0);
                    dayPane.getChildren().add(creerBlocSemaine(c, colWidth - 8, hCell, startY));
                }
            }
            body.getChildren().add(dayPane);
        }
        mainGrid.getChildren().add(body);
        planningContainer.getChildren().add(mainGrid);
    }

    private VBox creerBlocSemaine(Cours c, double width, double height, double y) {
        VBox bloc = new VBox(2);
        bloc.setLayoutX(4); bloc.setLayoutY(y);
        bloc.setPrefSize(width, height - 2);

        String color = getHexColor(c);
        bloc.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 8; -fx-padding: 8; -fx-cursor: hand;");
        bloc.setOnMouseClicked(e -> afficherDetailsCours(c));

        Label code = new Label(c.getIntitule().toUpperCase() + " (" + c.getTypeCours().name() + ")");
        code.setStyle("-fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 10;");
        code.setWrapText(true);

        Label salle = new Label("📍 " + c.getSalle().getNumero());
        salle.setStyle("-fx-text-fill: white; -fx-font-size: 9;");

        Label prof = new Label("👤 " + obtenirNomProf(c));
        prof.setStyle("-fx-text-fill: white; -fx-font-size: 9; -fx-opacity: 0.9;");

        bloc.getChildren().addAll(code, salle, prof);
        return bloc;
    }

    private void chargerVueJourSemaine(LocalDate date) {
        planningContainer.getChildren().clear();
        LocalDate debut = date.with(DayOfWeek.MONDAY);
        LocalDate fin = debut.plusDays(6);

        List<Cours> tousLesCours = recupererCoursFiltres(debut, fin);

        FlowPane gridDays = new FlowPane();
        gridDays.setHgap(20); gridDays.setVgap(20);
        gridDays.setPadding(new Insets(10));
        gridDays.setPrefWrapLength(1200);

        for (int i = 0; i < 7; i++) {
            LocalDate jour = debut.plusDays(i);
            VBox dayColumn = new VBox(15);
            dayColumn.setPrefWidth(380); dayColumn.setMinWidth(380);

            boolean isHighlighted = jour.equals(LocalDate.now());
            String borderColor = isHighlighted ? "#3B82F6" : "#E5E7EB";

            dayColumn.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 25; -fx-background-radius: 20; -fx-border-color: " + borderColor + "; -fx-border-width: 1;");

            Label title = new Label(capitalize(jour.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH)) + " " + jour.getDayOfMonth() + "/" + jour.getMonthValue());
            title.setStyle("-fx-font-size: 20; -fx-font-weight: 900; -fx-text-fill: " + (isHighlighted ? "#3B82F6" : "#111827") + ";");
            dayColumn.getChildren().add(title);

            List<Cours> coursDuJour = tousLesCours.stream()
                    .filter(c -> c.getDateDebut().toLocalDate().equals(jour))
                    .collect(Collectors.toList());

            if (coursDuJour.isEmpty()) {
                Label vide = new Label("Aucun cours");
                vide.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic;");
                dayColumn.getChildren().add(vide);
            } else {
                for (Cours c : coursDuJour) {
                    dayColumn.getChildren().add(creerCarteDetailExacte(c));
                }
            }
            gridDays.getChildren().add(dayColumn);
        }
        planningContainer.getChildren().add(gridDays);
    }

    private VBox creerCarteDetailExacte(Cours c) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-padding: 20;");

        HBox rootLayout = new HBox(15);
        rootLayout.setAlignment(Pos.TOP_LEFT);

        Region sideBar = new Region();
        sideBar.setMinWidth(4);
        sideBar.setStyle("-fx-background-color: " + getHexColor(c) + "; -fx-background-radius: 2;");

        VBox content = new VBox(10);
        Label title = new Label(c.getIntitule());
        title.setStyle("-fx-font-size: 17; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label badge = new Label(getTypeLabel(c));
        badge.setStyle("-fx-background-color: " + getBadgeBgColor(c) + "; -fx-text-fill: " + getHexColor(c) + "; -fx-padding: 4 12; -fx-background-radius: 10; -fx-font-size: 11; -fx-font-weight: bold;");

        VBox infoBox = new VBox(8);
        infoBox.getChildren().addAll(
                creerInfoLigne("🕒", c.getDateDebut().toLocalTime() + " - " + c.getDateFin().toLocalTime()),
                creerInfoLigne("📍", c.getSalle().getNumero()),
                creerInfoLigne("👤", obtenirNomProf(c))
        );

        content.getChildren().addAll(title, badge, infoBox);
        rootLayout.getChildren().addAll(sideBar, content);
        card.getChildren().add(rootLayout);

        return card;
    }

    private HBox creerInfoLigne(String icon, String text) {
        HBox line = new HBox(10);
        line.setAlignment(Pos.CENTER_LEFT);
        line.getChildren().addAll(new Label(icon), new Label(text));
        return line;
    }

    private String getHexColor(Cours c) {
        return c.getTypeCours().name().equals("CM") ? "#3B82F6" : "#10B981";
    }

    private String getBadgeBgColor(Cours c) {
        return c.getTypeCours().name().equals("CM") ? "#EFF6FF" : "#ECFDF5";
    }

    private void afficherDetailsCours(Cours c) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du cours");
        alert.setHeaderText(c.getIntitule());
        alert.setContentText("Type : " + getTypeLabel(c) + "\nSalle : " + c.getSalle().getNumero() +
                "\nHeure : " + c.getDateDebut().toLocalTime() + " - " + c.getDateFin().toLocalTime() +
                "\nResponsable : " + obtenirNomProf(c));
        alert.show();
    }

    @FXML private void onPrevWeek() { currentDate = currentDate.minusWeeks(1); chargerDonnees(); }
    @FXML private void onNextWeek() { currentDate = currentDate.plusWeeks(1); chargerDonnees(); }
    @FXML private void onWeekView() { isWeekView = true; updateToggleButtonStyles(); chargerDonnees(); }
    @FXML private void onDayView() { isWeekView = false; updateToggleButtonStyles(); chargerDonnees(); }

    private void updateToggleButtonStyles() {
        String active = "-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 0); -fx-font-weight: bold; -fx-text-fill: #111827;";
        String inactive = "-fx-background-color: transparent; -fx-text-fill: #9CA3AF;";
        btnWeek.setStyle(isWeekView ? active : inactive);
        btnDay.setStyle(!isWeekView ? active : inactive);
    }

    private String capitalize(String s) { return s.substring(0,1).toUpperCase() + s.substring(1); }
    private String getTypeLabel(Cours c) { return c.getTypeCours().name().equals("CM") ? "Cours Magistral" : "Travaux Dirigés / Pratiques"; }
}