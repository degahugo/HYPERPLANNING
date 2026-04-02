package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.dao.CoursDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.time.LocalDate;

public class DashboardAdminController {

    @FXML private Label totalCoursLabel;
    @FXML private Label coursSemaineLabel;

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
    }


    @FXML
    private void ouvrirRechercheSalles() {
        MainController.getInstance().gererNavigation("SALLES");
    }

    @FXML
    private void ouvrirGestionConflits() {
        MainController.getInstance().gererNavigation("CONFLITS");
    }

    @FXML
    private void ajouterCours() { MainController.getInstance().gererNavigation("AJOUTER_COURS"); }
}