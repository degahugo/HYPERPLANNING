package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.dao.SalleDAO;
import fr.univ.hyperplanning.model.pedagogie.Salle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class SallesDispoController {

    @FXML private DatePicker datePicker;
    @FXML private TextField heureDebut;
    @FXML private TextField heureFin;
    @FXML private TableView<Salle> tableSalles;

    // Colonnes alignées sur ton entité Salle
    @FXML private TableColumn<Salle, String> colNumero;
    @FXML private TableColumn<Salle, String> colBatiment;
    @FXML private TableColumn<Salle, Integer> colCapacite;

    private SalleDAO salleDAO;

    @FXML
    public void initialize() {
        // CORRECTION : On utilise "numero" car "nom" n'existe pas dans ton entité Salle
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colBatiment.setCellValueFactory(new PropertyValueFactory<>("batiment"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));
    }

    public void setSalleDAO(SalleDAO salleDAO) {
        this.salleDAO = salleDAO;
    }

    @FXML
    private void handleRecherche() {
        try {
            if (datePicker.getValue() == null || heureDebut.getText().isEmpty() || heureFin.getText().isEmpty()) {
                showAlert("Champs vides", "Veuillez remplir la date et les heures.");
                return;
            }

            // Conversion des saisies
            LocalDateTime debut = LocalDateTime.of(datePicker.getValue(), LocalTime.parse(heureDebut.getText()));
            LocalDateTime fin = LocalDateTime.of(datePicker.getValue(), LocalTime.parse(heureFin.getText()));

            if (salleDAO != null) {
                List<Salle> disponibles = salleDAO.findSallesDisponibles(debut, fin);
                tableSalles.setItems(FXCollections.observableArrayList(disponibles));
            }
        } catch (Exception e) {
            showAlert("Erreur de format", "L'heure doit être au format HH:mm (ex: 08:30)");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}