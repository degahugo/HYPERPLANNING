package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.dao.CoursDAO;
import fr.univ.hyperplanning.dao.SalleDAO;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.pedagogie.Salle;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.List;

public class AffecterSalleController {

    @FXML
    private Label coursLabel;

    @FXML
    private ComboBox<Salle> salleComboBox;

    private Cours cours; // le cours sur lequel on travaille

    private final CoursDAO coursDAO = new CoursDAO();
    private final SalleDAO salleDAO = new SalleDAO(JpaUtil.em());

    public void setCours(Cours cours) {
        this.cours = cours;
        coursLabel.setText("Cours : " + cours.getIntitule());

        // Charger toutes les salles
        List<Salle> salles = salleDAO.findAll();
        salleComboBox.getItems().setAll(salles);  @FXML
        private void onValider() {
            String salleChoisie = comboSalles.getValue();
            if (salleChoisie == null) return;

            // TODO : appeler le DAO pour enregistrer la salle dans le cours
            System.out.println("Salle choisie : " + salleChoisie);
        }
    }


    @FXML
    private void validerAffectation() {
        Salle salleSelectionnee = salleComboBox.getValue();
        if (salleSelectionnee == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez choisir une salle !");
            alert.showAndWait();
            return;
        }

        try {
            // Affectation en base
            coursDAO.affecterSalle(cours.getId(), salleSelectionnee.getId());

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Salle affectée avec succès !");
            alert.showAndWait();

            // Fermer la fenêtre
            Stage stage = (Stage) coursLabel.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage());
            alert.showAndWait();
        }
    }
}