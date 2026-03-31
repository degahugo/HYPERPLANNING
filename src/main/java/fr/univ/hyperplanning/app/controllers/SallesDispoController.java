package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.dao.SalleDAO;
import fr.univ.hyperplanning.model.pedagogie.Salle;
import fr.univ.hyperplanning.service.ConflictService; // Import nécessaire
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SallesDispoController {

    @FXML private DatePicker datePicker;
    @FXML private TextField heureDebut;
    @FXML private TextField heureFin;
    @FXML private TableView<Salle> tableSalles;
    @FXML private Button btnValiderResolution; // Injecté depuis le FXML

    @FXML private TableColumn<Salle, String> colNumero;
    @FXML private TableColumn<Salle, String> colBatiment;
    @FXML private TableColumn<Salle, Integer> colCapacite;

    private SalleDAO salleDAO = new SalleDAO(); // Initialisé pour le test
    private final ConflictService conflictService = new ConflictService();

    @FXML
    public void initialize() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colBatiment.setCellValueFactory(new PropertyValueFactory<>("batiment"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));

        // Vérifier si on est en mode "Résolution de conflit"
        if (ConflictController.selectedCoursIdForResolution != null) {
            btnValiderResolution.setVisible(true);
            System.out.println("Mode résolution actif pour le cours ID: " + ConflictController.selectedCoursIdForResolution);
        }
    }

    public void setSalleDAO(SalleDAO salleDAO) {
        this.salleDAO = salleDAO;
    }

    @FXML
    private void handleRecherche() {
        try {
            String hDebutRaw = heureDebut.getText().trim();
            String hFinRaw = heureFin.getText().trim();

            if (datePicker.getValue() == null || hDebutRaw.isEmpty() || hFinRaw.isEmpty()) {
                showAlert("Champs vides", "Veuillez remplir la date et les heures.");
                return;
            }

            DateTimeFormatter souple = DateTimeFormatter.ofPattern("H:mm");
            LocalTime timeDebut = LocalTime.parse(hDebutRaw, souple);
            LocalTime timeFin = LocalTime.parse(hFinRaw, souple);

            LocalDateTime debut = LocalDateTime.of(datePicker.getValue(), timeDebut);
            LocalDateTime fin = LocalDateTime.of(datePicker.getValue(), timeFin);

            if (salleDAO != null) {
                List<Salle> disponibles = salleDAO.findSallesDisponibles(debut, fin);
                tableSalles.setItems(FXCollections.observableArrayList(disponibles));

                if (disponibles.isEmpty()) {
                    showAlert("Information", "Aucune salle n'est disponible sur ce créneau.");
                }
            }
        } catch (java.time.format.DateTimeParseException e) {
            showAlert("Erreur de format", "L'heure doit être au format HH:mm (ex: 08:30)");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur inattendue est survenue.");
        }
    }

    /**
     * Méthode appelée par le bouton vert "Assigner la salle"
     * Résout l'erreur rouge dans le FXML
     */
    @FXML
    private void handleConfirmResolution() {
        Salle salleChoisie = tableSalles.getSelectionModel().getSelectedItem();
        Long coursId = ConflictController.selectedCoursIdForResolution;

        if (salleChoisie == null) {
            showAlert("Sélection requise", "Veuillez choisir une salle dans le tableau.");
            return;
        }

        try {
            // Récupération des horaires pour mettre à jour le cours
            DateTimeFormatter souple = DateTimeFormatter.ofPattern("H:mm");
            LocalDateTime debut = LocalDateTime.of(datePicker.getValue(), LocalTime.parse(heureDebut.getText().trim(), souple));
            LocalDateTime fin = LocalDateTime.of(datePicker.getValue(), LocalTime.parse(heureFin.getText().trim(), souple));

            // Appel au service de résolution
            boolean succes = conflictService.resolveConflict(coursId, salleChoisie.getId(), debut, fin);

            if (succes) {
                // Nettoyage de la variable statique
                ConflictController.selectedCoursIdForResolution = null;

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText("Conflit Résolu !");
                successAlert.setContentText("Le cours a été déplacé en salle " + salleChoisie.getNumero());
                successAlert.showAndWait();

                // Retour à la vue précédente
                returnToConflictView();
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la validation : " + e.getMessage());
        }
    }

    private void returnToConflictView() {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fr/univ/hyperplanning/app/views/conflict_view.fxml"));
            tableSalles.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
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