package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.app.SessionUtilisateur;
import fr.univ.hyperplanning.dao.CoursDAO;
import fr.univ.hyperplanning.model.pedagogie.Cours;
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
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.util.List;

public class DashboardEnseignantController {

    @FXML private Label totalCoursLabel;
    @FXML private Label coursMoisLabel;
    @FXML private Label coursJourLabel;
    @FXML private VBox  todayLessonsContainer;

    @FXML private VBox cardMesAbsences;
    @FXML private VBox cardAbsencesCours;
    @FXML private Label mesAbsencesCountLabel;
    @FXML private Label absencesCoursCountLabel;

    private final AbsenceDAO absenceDAO = new AbsenceDAO();



    private final CoursDAO coursDAO = new CoursDAO();

    @FXML
    public void initialize() {
        LocalDate now = LocalDate.now();
        Utilisateur user = SessionUtilisateur.get();

        if (user == null) return;


        List<Cours> tous = coursDAO.findAllByEnseignant(user.getId());
        List<Cours> mois = coursDAO.findByEnseignant(user.getId(),
                now.withDayOfMonth(1), now.withDayOfMonth(now.lengthOfMonth()));
        List<Cours> jour = coursDAO.findByEnseignant(user.getId(), now, now);

        // Mise à jour des textes
        totalCoursLabel.setText(String.valueOf(tous.size()));
        if (coursMoisLabel != null) coursMoisLabel.setText(String.valueOf(mois.size()));
        if (coursJourLabel != null) coursJourLabel.setText(String.valueOf(jour.size()));

        // Affichage des cartes du jour
        todayLessonsContainer.getChildren().clear();
        for (Cours c : jour) {
            todayLessonsContainer.getChildren().add(creerCard(c));
        }

        initialiserCartesAbsences(user);
    }

    private void initialiserCartesAbsences(Utilisateur user) {
        if (user == null) return;

        if (mesAbsencesCountLabel != null) {
            mesAbsencesCountLabel.setText(String.valueOf(
                    absenceDAO.countAbsencesByUtilisateur(user.getId())
            ));
        }

        if (absencesCoursCountLabel != null) {
            absencesCoursCountLabel.setText(String.valueOf(
                    absenceDAO.countAbsencesByEnseignant(user.getId())
            ));
        }

        if (cardMesAbsences != null) {
            cardMesAbsences.setOnMouseClicked(e -> {
                if (MainController.getInstance() != null) {
                    MainController.getInstance().gererNavigation("MES_ABSENCES");
                }
            });
        }

        if (cardAbsencesCours != null) {
            cardAbsencesCours.setOnMouseClicked(e -> {
                if (MainController.getInstance() != null) {
                    MainController.getInstance().gererNavigation("ABSENCES_COURS");
                }
            });
        }
    }


    private VBox creerCard(Cours c) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 2); " +
                "-fx-border-color: #F3F4F6; -fx-border-radius: 12;");

        Label titre = new Label(c.getIntitule());
        titre.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label salle = new Label("📍  " + (c.getSalle() != null ? c.getSalle().getNumero() : "N/A"));
        salle.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label heure = new Label(c.getDateDebut().toLocalTime() + " – " + c.getDateFin().toLocalTime());
        heure.setStyle("-fx-text-fill: #8B5CF6; -fx-font-weight: bold; -fx-font-size: 12;");

        HBox footer = new HBox(salle, spacer, heure);
        footer.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(titre, footer);
        return card;
    }

    @FXML
    private void handleVoirToutPlanning() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().gererNavigation("PLANNING");
        }
    }

    @FXML
    private void ouvrirRechercheSalles() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().gererNavigation("SALLES");
        }
    }
}