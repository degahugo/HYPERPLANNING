package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.model.ConflictInfo;
import fr.univ.hyperplanning.service.ConflictService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ConflictController {
    @FXML
    private TableView<ConflictInfo> conflictTable;
    @FXML
    private TableColumn<ConflictInfo, Long> colId;
    @FXML
    private TableColumn<ConflictInfo, String> colType; // AJOUT : Pour voir si c'est une salle ou un groupe
    @FXML
    private TableColumn<ConflictInfo, String> colDescription;

    private final ConflictService conflictService = new ConflictService();

    @FXML
    public void initialize() {
        // Liaison aux attributs de ConflictInfo
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type")); // AJOUT
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        // OPTIONNEL : Lancer la détection dès l'ouverture pour que l'utilisateur
        // n'ait pas à cliquer sur un bouton (Résultat attendu : consulter la liste)
        handleDetectConflicts();
    }

    @FXML
    public void handleDetectConflicts() {
        System.out.println("Détection automatique en cours...");
        // On utilise getAllConflicts (la méthode qu'on a enrichie à la tâche 1)
        ObservableList<ConflictInfo> conflicts = FXCollections.observableArrayList(conflictService.getAllConflicts());
        conflictTable.setItems(conflicts);
    }

    // --- NOUVELLE MÉTHODE POUR L'UC6.3 ---
    @FXML
    private void handleResolveConflict() {
        // 1. On récupère le conflit sélectionné dans le tableau
        ConflictInfo selected = conflictTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // --- APPEL DE LA TÂCHE 5 ---
            // On imagine qu'on a récupéré l'ID de la nouvelle salle (ex: 10L)
            boolean isModified = conflictService.resolveConflict(selected.getId(), 10L, null, null);

            if (isModified) {
                // --- DÉBUT DE LA TÂCHE 6 ---

                // Détail technique : Relancer le service de détection
                // On appelle la méthode qui va interroger à nouveau la base de données
                handleDetectConflicts();

                // Résultat attendu :
                // Si la nouvelle salle n'est plus en conflit, handleDetectConflicts()
                // va mettre à jour la liste observable et le conflit DISPARAÎTRA du tableau.

                System.out.println("Vérification terminée : Le conflit a été recalculé.");
            }
        }
    }
}