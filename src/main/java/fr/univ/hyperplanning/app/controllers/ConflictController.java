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
    private TableColumn<ConflictInfo, String> colDescription;
    @FXML
    private final ConflictService conflictService = new ConflictService();

    @FXML
    public void initialize() {
        // On lie les colonnes aux attributs de ConflictInfo
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
    }


    @FXML
    public void handleDetectConflicts() {
        System.out.println("Détection en cours...");
        // On récupère la liste des conflits depuis le service
        ObservableList<ConflictInfo> conflicts = FXCollections.observableArrayList(conflictService.getRoomConflicts());
        conflictTable.setItems(conflicts);
    }
}