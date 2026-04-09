package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.dao.AbsenceDAO;
import fr.univ.hyperplanning.dao.CoursDAO;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import java.time.LocalDate;

public class DashboardAdminController {

    @FXML private Label totalCoursLabel;
    @FXML private Label coursSemaineLabel;
    @FXML private VBox cardAbsencesATraiter;
    @FXML private Label absencesATraiterCountLabel;

    private final AbsenceDAO absenceDAO = new AbsenceDAO();
    @FXML
    private void ouvrirGestionAbsences() {
        MainController.getInstance().gererNavigation("ABSENCES");
    }
    private final CoursDAO coursDAO = new CoursDAO();

    @FXML
    public void initialize() {
        try {
            int total = coursDAO.findAll().size();
            int semaine = coursDAO.findByWeek(LocalDate.now()).size();

            if (totalCoursLabel != null) totalCoursLabel.setText(String.valueOf(total));
            if (coursSemaineLabel != null) coursSemaineLabel.setText(String.valueOf(semaine));

        } catch (Exception e) {
            e.printStackTrace();
        }

        initialiserCarteAbsences();
    }

    private void initialiserCarteAbsences() {
        if (absencesATraiterCountLabel != null) {
            absencesATraiterCountLabel.setText(String.valueOf(
                    absenceDAO.countAbsencesAValider()
            ));
        }

        if (cardAbsencesATraiter != null) {
            cardAbsencesATraiter.setOnMouseClicked(e -> {
                if (MainController.getInstance() != null) {
                    MainController.getInstance().gererNavigation("GESTION_ABSENCES");
                }
            });
        }
    }


    @FXML
    private void ouvrirRechercheSalles() {
        MainController.getInstance().gererNavigation("SALLES");
    }

    @FXML
    private void ouvrirGestionConflits() {
        MainController.getInstance().gererNavigation("CONFLITS");
    }

    /**
     * Ouvre le fxml pour ajouter un cours
     */
    @FXML
    private void ajouterCours() { MainController.getInstance().gererNavigation("AJOUTER_COURS"); }
}