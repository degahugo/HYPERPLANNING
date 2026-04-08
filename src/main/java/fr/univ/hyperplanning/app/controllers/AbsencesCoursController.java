package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.app.SessionUtilisateur;
import fr.univ.hyperplanning.dao.AbsenceDAO;
import fr.univ.hyperplanning.model.enums.RoleUtilisateur;
import fr.univ.hyperplanning.model.gestion.Absence;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class AbsencesCoursController {

    @FXML private TableView<Absence> tableAbsences;
    @FXML private TableColumn<Absence, String> colNom;
    @FXML private TableColumn<Absence, String> colPrenom;
    @FXML private TableColumn<Absence, String> colUtilisateurs;
    @FXML private TableColumn<Absence, String> colCours;
    @FXML private TableColumn<Absence, String> colDate;
    @FXML private TableColumn<Absence, String> colMotif;
    @FXML private TableColumn<Absence, String> colStatut;

    private final AbsenceDAO absenceDAO = new AbsenceDAO();

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerDonnees();
    }

    private void configurerColonnes() {
        colNom.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getUtilisateurAbsent() != null ? data.getValue().getUtilisateurAbsent().getNom() : ""
                ));

        colPrenom.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getUtilisateurAbsent() != null ? data.getValue().getUtilisateurAbsent().getPrenom() : ""
                ));

        colUtilisateurs.setCellValueFactory(data -> {
            Utilisateur u = data.getValue().getUtilisateurAbsent();
            String role = switch (u.getRole()) {
                case ETUDIANT -> "Étudiant";
                case ENSEIGNANT, ENSEIGNANT_RESPONSABLE -> "Enseignant";
                case INTERVENANT -> "Intervenant";
                default -> "Utilisateur";
            };
            return new SimpleStringProperty(role);
        });

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
    }

    private void chargerDonnees() {
        Utilisateur user = SessionUtilisateur.get();
        if (user == null) return;

        List<Absence> absences = switch (user.getRole()) {
            case ENSEIGNANT, ENSEIGNANT_RESPONSABLE ->
                    absenceDAO.findAbsencesByEnseignant(user.getId());
            case INTERVENANT ->
                    absenceDAO.findAbsencesByIntervenant(user.getId());
            default -> List.of();
        };

        tableAbsences.setItems(FXCollections.observableArrayList(absences));
    }
}