package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.model.ConflictInfo;
import fr.univ.hyperplanning.service.ConflictService;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.util.List;
import javafx.scene.control.Alert;

public class ConflictController {

    public static Long selectedCoursIdForResolution;

    @FXML
    private TableView<ConflictInfo> conflictTable;

    @FXML
    private TableColumn<ConflictInfo, Long> colId;

    @FXML
    private TableColumn<ConflictInfo, String> colType;

    @FXML
    private TableColumn<ConflictInfo, String> colDescription;

    private final ConflictService conflictService = new ConflictService();

    /**
     * Cette méthode fait le pont entre le FXML et les données de ConflictInfo.
     * On utilise des Lambdas pour garantir que JavaFX trouve bien les Getters.
     */
    @FXML
    public void initialize() {
        // Liaison de la colonne ID (Convertit le long primitif en Long objet pour JavaFX)
        colId.setCellValueFactory(cellData ->
                new SimpleLongProperty(cellData.getValue().getId()).asObject());

        // Liaison de la colonne Type
        colType.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getType()));

        // Liaison de la colonne Description
        colDescription.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescription()));

        // On lance la détection immédiatement à l'affichage de la page
        handleDetectConflicts();
    }

    /**
     * Déclenche la recherche de conflits via le service et remplit le tableau.
     */
    @FXML
    public void handleDetectConflicts() {
        System.out.println("--- Début de la détection ---");

        // Sécurité : Vérifier si le FXML a bien injecté le tableau
        if (conflictTable == null) {
            System.err.println("❌ ERREUR : conflictTable est null. Vérifiez l'fx:id dans le FXML.");
            return;
        }

        List<ConflictInfo> results = conflictService.getAllConflicts();
        System.out.println("DEBUG : Nombre de conflits trouvés : " + results.size());

        ObservableList<ConflictInfo> conflicts = FXCollections.observableArrayList(results);
        conflictTable.setItems(conflicts);
        conflictTable.refresh();
    }

    /**
     * Logique de résolution (Tâche 5 & 6)
     */
    @FXML
    private void handleResolveConflict() {
        // 1. On récupère le conflit sélectionné dans le tableau
        ConflictInfo selected = conflictTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // 2. On stocke l'ID du cours dans la variable statique pour l'autre page
            selectedCoursIdForResolution = selected.getId();

            System.out.println("🔄 Redirection vers UC4 pour résoudre le cours ID : " + selectedCoursIdForResolution);

            try {
                // 3. Chargement de la vue UC4 (Salles Disponibles)
                // Vérifie bien le chemin vers ton fichier FXML
                javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/SallesDispoView.fxml"));

                // 4. On change le contenu de la fenêtre
                conflictTable.getScene().setRoot(root);

            } catch (java.io.IOException e) {
                e.printStackTrace();
                System.out.println("❌ Erreur : Impossible de charger la page des salles disponibles.");
            }
        } else {
            // Si l'utilisateur n'a pas cliqué sur une ligne du tableau
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez sélectionner un conflit dans le tableau avant de cliquer sur Résoudre.");
            alert.show();
        }
    }
}