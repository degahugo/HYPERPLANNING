package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.service.ConflictService;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.io.IOException;

public class ModifHoraireController {

    @FXML private DatePicker datePicker;

    // Changement des noms pour correspondre au FXML (Field à la fin)
    @FXML private TextField heureDebutField;
    @FXML private TextField heureFinField;

    private final ConflictService service = new ConflictService();

    @FXML
    private void handleValider() {
        try {
            // 1. Récupérer l'ID stocké
            Long id = ConflictController.selectedCoursIdForResolution;

            if (datePicker.getValue() == null || heureDebutField.getText().isEmpty() || heureFinField.getText().isEmpty()) {
                showError("Champs vides", "Veuillez remplir tous les champs.");
                return;
            }

            // 2. Transformer la saisie
            LocalDate date = datePicker.getValue();
            LocalTime debutTime = LocalTime.parse(heureDebutField.getText());
            LocalTime finTime = LocalTime.parse(heureFinField.getText());

            LocalDateTime debut = date.atTime(debutTime);
            LocalDateTime fin = date.atTime(finTime);

            // 3. Appel du service
            boolean succes = service.rescheduleCours(id, debut, fin);

            if (succes) {
                showInformation("Succès", "L'horaire a été mis à jour. Le conflit est résolu.");
                retournerAuxConflits();
            }
        } catch (Exception e) {
            showError("Format invalide", "L'heure doit être au format HH:mm (ex: 08:30).");
        }
    }

    // CETTE MÉTHODE MANQUAIT ET CAUSAIT L'ERREUR ROUGE
    @FXML
    private void handleAnnuler() {
        retournerAuxConflits();
    }

    private void retournerAuxConflits() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/ConflictView.fxml"));
            datePicker.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur redirection : " + e.getMessage());
        }
    }

    private void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}