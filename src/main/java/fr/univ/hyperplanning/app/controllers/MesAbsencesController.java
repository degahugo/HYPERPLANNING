package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.app.SessionUtilisateur;
import fr.univ.hyperplanning.dao.AbsenceDAO;
import fr.univ.hyperplanning.model.gestion.Absence;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class MesAbsencesController {

    @FXML private TableView<Absence> tableAbsences;
    @FXML private TableColumn<Absence, String> colCours;
    @FXML private TableColumn<Absence, String> colDate;
    @FXML private TableColumn<Absence, String> colMotif;
    @FXML private TableColumn<Absence, String> colStatut;
    @FXML private TableColumn<Absence, String> colDateDeclaration;
    @FXML private TableColumn<Absence, String> colDateValidation;
    @FXML private TableColumn<Absence, String> colMotifRefus;

    private final AbsenceDAO absenceDAO = new AbsenceDAO();

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerDonnees();
    }

    private void configurerColonnes() {
        colCours.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCours() != null ? data.getValue().getCours().getIntitule() : "N/A"
                ));

        colDate.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getDateAbsence() != null ? data.getValue().getDateAbsence().toString() : ""
                ));

        colMotif.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getMotif() != null ? data.getValue().getMotif() : ""
                ));

        colStatut.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getStatut() != null ? data.getValue().getStatut().name() : ""
                ));

        colDateDeclaration.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getDateDeclaration() != null ? data.getValue().getDateDeclaration().toString() : ""
                ));

        colDateValidation.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getDateValidation() != null ? data.getValue().getDateValidation().toString() : ""
                ));

        colMotifRefus.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getMotifRefus() != null ? data.getValue().getMotifRefus() : ""
                ));
    }

    private void chargerDonnees() {
        Utilisateur user = SessionUtilisateur.get();
        if (user == null) return;

        tableAbsences.setItems(
                FXCollections.observableArrayList(absenceDAO.findAbsencesByUtilisateur(user.getId()))
        );
    }
}