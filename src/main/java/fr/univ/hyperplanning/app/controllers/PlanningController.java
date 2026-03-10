package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.dao.CoursDAO;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class PlanningController {

    private final CoursDAO coursDAO = new CoursDAO();

    @FXML
    private TableView<Cours> coursTable;

    @FXML
    private TableColumn<Cours, String> colJour;

    @FXML
    private TableColumn<Cours, String> colHeureDebut;

    @FXML
    private TableColumn<Cours, String> colHeureFin;

    @FXML
    private TableColumn<Cours, String> colDuree;

    @FXML
    private TableColumn<Cours, String> colModule;

    @FXML
    private TableColumn<Cours, String> colSalle;

    @FXML
    private TableColumn<Cours, String> colEnseignant;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Label weekLabel;

    private LocalDate currentDate;

    @FXML
    public void initialize() {

        System.out.println("PlanningController chargé");

        currentDate = LocalDate.now();
        datePicker.setValue(currentDate);

        initialiserColonnes();

        chargerSemaine(currentDate);

        coursTable.setRowFactory(tv -> {
            TableRow<Cours> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {

                    Cours cours = row.getItem();
                    afficherDetailsCours(cours);

                }
            });

            return row;
        });
    }

    private void initialiserColonnes() {

        colJour.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        cell.getValue().getDateDebut() != null
                                ? cell.getValue().getDateDebut()
                                .toLocalDate()
                                .getDayOfWeek()
                                .getDisplayName(
                                        java.time.format.TextStyle.FULL,
                                        java.util.Locale.FRENCH
                                )
                                : ""
                )
        );

        colHeureDebut.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getDateDebut() != null
                                ? cell.getValue().getDateDebut().toString()
                                : ""
                )
        );

        colHeureFin.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getDateFin() != null
                                ? cell.getValue().getDateFin().toString()
                                : ""
                )
        );

        colDuree.setCellValueFactory(cell -> {

            int minutes = cell.getValue().getDuree();

            int heures = minutes / 60;
            int reste = minutes % 60;

            return new javafx.beans.property.SimpleStringProperty(
                    heures + "h" + (reste > 0 ? reste : "")
            );
        });

        colModule.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getModule() != null
                                ? cell.getValue().getModule().getNom()
                                : ""
                )
        );

        colSalle.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getSalle() != null
                                ? cell.getValue().getSalle().getNumero()
                                : ""
                )
        );

        colEnseignant.setCellValueFactory(cell ->
                new SimpleStringProperty("Non renseigné")
        );
    }

    private void afficherDetailsCours(Cours cours) {

        String module = cours.getModule() != null ? cours.getModule().getNom() : "Non renseigné";

        String salle = cours.getSalle() != null ? cours.getSalle().getNumero() : "Non renseigné";

        String type = cours.getTypeCours() != null ? cours.getTypeCours().toString() : "Non renseigné";

        String debut = cours.getDateDebut() != null ? cours.getDateDebut().toString() : "";

        String fin = cours.getDateFin() != null ? cours.getDateFin().toString() : "";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Détail du cours");

        alert.setHeaderText(module);

        alert.setContentText(
                "Type : " + type + "\n\n" +
                        "Salle : " + salle + "\n\n" +
                        "Début : " + debut + "\n\n" +
                        "Fin : " + fin
        );

        alert.showAndWait();
    }

    private void chargerSemaine(LocalDate date) {

        LocalDate debut = date.with(DayOfWeek.MONDAY);
        LocalDate fin = date.with(DayOfWeek.SUNDAY);

        weekLabel.setText("Semaine : " + debut + " → " + fin);

        coursTable.getItems().setAll(
                coursDAO.findByDateRange(debut, fin)
        );
    }

    @FXML
    private void onPrevWeek() {

        currentDate = currentDate.minusWeeks(1);
        datePicker.setValue(currentDate);

        chargerSemaine(currentDate);
    }

    @FXML
    private void onNextWeek() {

        currentDate = currentDate.plusWeeks(1);
        datePicker.setValue(currentDate);

        chargerSemaine(currentDate);
    }

    @FXML
    private void onDateChanged() {

        currentDate = datePicker.getValue();

        chargerSemaine(currentDate);
    }



}

