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
            // 1. On nettoie les saisies (enlève les espaces au début et à la fin)
            String hDebutRaw = heureDebut.getText().trim();
            String hFinRaw = heureFin.getText().trim();

            if (datePicker.getValue() == null || hDebutRaw.isEmpty() || hFinRaw.isEmpty()) {
                showAlert("Champs vides", "Veuillez remplir la date et les heures.");
                return;
            }

            // 2. On utilise un formateur qui accepte 8:00 ou 08:00
            java.time.format.DateTimeFormatter souple = java.time.format.DateTimeFormatter.ofPattern("H:mm");

            // 3. Conversion sécurisée
            LocalTime timeDebut = LocalTime.parse(hDebutRaw, souple);
            LocalTime timeFin = LocalTime.parse(hFinRaw, souple);

            LocalDateTime debut = LocalDateTime.of(datePicker.getValue(), timeDebut);
            LocalDateTime fin = LocalDateTime.of(datePicker.getValue(), timeFin);

            // 4. Lancement de la recherche
            if (salleDAO != null) {
                List<Salle> disponibles = salleDAO.findSallesDisponibles(debut, fin);
                tableSalles.setItems(FXCollections.observableArrayList(disponibles));

                if (disponibles.isEmpty()) {
                    showAlert("Information", "Aucune salle n'est disponible sur ce créneau.");
                }
            }
        } catch (java.time.format.DateTimeParseException e) {
            showAlert("Erreur de format", "L'heure doit être au format HH:mm (ex: 08:30 ou 8:30)");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur inattendue est survenue.");
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