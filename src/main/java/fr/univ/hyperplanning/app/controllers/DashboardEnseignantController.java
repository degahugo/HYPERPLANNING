package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.dao.CoursDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardEnseignantController {

    @FXML private Label totalCoursLabel;
    @FXML private Label demandesAttenteLabel;

    private final CoursDAO coursDAO = new CoursDAO();

    @FXML
    public void initialize() {
        totalCoursLabel.setText(String.valueOf(coursDAO.findAll().size()));
        demandesAttenteLabel.setText("3");
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