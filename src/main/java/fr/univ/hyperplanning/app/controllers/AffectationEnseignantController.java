package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.dao.CoursDAO;
import fr.univ.hyperplanning.dao.EnseignantDao;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.utilisateurs.Enseignant;
import fr.univ.hyperplanning.service.AffectationEnseignantService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import java.util.List;

public class AffectationEnseignantController {

    @FXML
    private ComboBox<Cours> coursComboBox;

    @FXML
    private ListView<Enseignant> enseignantsListView;

    @FXML
    private Label messageLabel;

    private final CoursDAO coursDAO = new CoursDAO();
    private final EnseignantDao enseignantDao = new EnseignantDao();
    private final AffectationEnseignantService affectationService = new AffectationEnseignantService();

    @FXML
    public void initialize() {
        coursComboBox.setItems(FXCollections.observableArrayList(coursDAO.findAll()));
        enseignantsListView.setItems(FXCollections.observableArrayList(enseignantDao.findAll()));
        enseignantsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        coursComboBox.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Cours item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getIntitule() + " - " + item.getDateDebut());
            }
        });

        coursComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Cours item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getIntitule() + " - " + item.getDateDebut());
            }
        });

        enseignantsListView.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Enseignant item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom() + " " + item.getPrenom());
            }
        });
    }

    @FXML
    public void handleEnregistrer() {
        Cours coursSelectionne = coursComboBox.getValue();
        List<Enseignant> enseignantsSelectionnes = enseignantsListView.getSelectionModel().getSelectedItems();

        if (coursSelectionne == null) {
            messageLabel.setText("Veuillez sélectionner un cours.");
            return;
        }

        if (enseignantsSelectionnes == null || enseignantsSelectionnes.isEmpty()) {
            messageLabel.setText("Veuillez sélectionner au moins un enseignant.");
            return;
        }

        List<Long> ids = enseignantsSelectionnes.stream()
                .map(Enseignant::getId)
                .toList();

        try {
            affectationService.affecterEnseignantsACours(coursSelectionne.getId(), ids);
            messageLabel.setText("Affectation enregistrée avec succès.");
        } catch (Exception e) {
            messageLabel.setText("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}