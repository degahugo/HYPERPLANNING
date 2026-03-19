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
    public void handleResolveConflict() {
        // 1. On récupère la ligne sélectionnée dans le tableau
        ConflictInfo selected = conflictTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            System.out.println("Résolution du conflit pour le cours ID : " + selected.getId());

            // 2. On change la salle (ID 2 pour le test, assure-toi qu'une salle ID 2 existe en base)
            boolean success = conflictService.updateCoursSalle(selected.getId(), 2L);

            if (success) {
                System.out.println("Succès ! Re-vérification des conflits...");
                // 3. REVALIDATION : On rafraîchit la liste pour montrer que le conflit a disparu
                handleDetectConflicts();
            } else {
                System.err.println("Échec de la mise à jour en base de données.");
            }
        } else {
            System.out.println("Veuillez sélectionner une ligne dans le tableau avant de cliquer.");
        }
    }
}