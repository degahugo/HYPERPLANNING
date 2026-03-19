package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.dao.EnseignantDAO;
import fr.univ.hyperplanning.model.utilisateurs.Enseignant;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class SelectionEnseignantController {

    @FXML
    private TableView<Enseignant> tableEnseignants;

    @FXML
    private TableColumn<Enseignant, String> colNom;

    @FXML
    private TableColumn<Enseignant, String> colPrenom;

    @FXML
    private TableColumn<Enseignant, String> colSpecialite;

    private EnseignantDAO enseignantDAO = new EnseignantDAO();

    @FXML
    public void initialize(){

        colNom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNom()));
        colPrenom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPrenom()));
        colSpecialite.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSpecialite()));

        tableEnseignants.setItems(
                FXCollections.observableArrayList(enseignantDAO.findAll())
        );
    }

    @FXML
    public void affecterEnseignant(){

        Enseignant selected = tableEnseignants.getSelectionModel().getSelectedItem();

        if(selected != null){
            System.out.println("Enseignant sélectionné : " + selected.getNom());
        }
    }
}