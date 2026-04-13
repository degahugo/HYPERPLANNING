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
            System.err.println("ERREUR : conflictTable est null. Vérifiez l'fx:id dans le FXML.");
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
            // 2. On stocke l'ID du cours pour que la page suivante sache quoi modifier
            selectedCoursIdForResolution = selected.getId();

            try {
                String fxmlPath;

                // 3. LE CHOIX DU CHEMIN (Ton bloc de code)
                // Si c'est un problème de groupe, on va changer l'heure
                if ("GROUPE".equals(selected.getType())) {
                    System.out.println("Conflit de Groupe : Direction Modification Horaire");
                    fxmlPath = "/fxml/ModifHoraireView.fxml";
                }
                // Sinon (si c'est SALLE ou autre), on va chercher une salle vide
                else {
                    System.out.println("Conflit de Salle : Direction Salles Disponibles");
                    fxmlPath = "/fxml/SallesDispoView.fxml";
                }

                // 4. Chargement et affichage de la nouvelle vue
                javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource(fxmlPath));
                conflictTable.getScene().setRoot(root);

            } catch (java.io.IOException e) {
                e.printStackTrace();
                System.err.println("Erreur : Impossible de charger le fichier FXML : " + e.getMessage());
            }
        } else {
            // Si rien n'est sélectionné
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez sélectionner un conflit dans le tableau.");
            alert.show();
        }
    }
}