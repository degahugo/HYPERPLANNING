package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.dao.CoursDAO;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import fr.univ.hyperplanning.dao.SalleDAO;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import java.time.format.TextStyle;
import java.util.List;
import javafx.scene.control.*;
import javafx.scene.control.ComboBox;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Locale;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.app.controllers.SallesDispoController;

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
    private void onWeekView(){
        chargerSemaine(currentDate);
    }

    @FXML
    private Label weekLabel;

    private LocalDate currentDate;


    @FXML
    private ComboBox<String> filterType;

    @FXML
    private ComboBox<String> filterValue;

    @FXML
    public void initialize() {

        System.out.println("PlanningController chargé");

        currentDate = LocalDate.now();

        // Dans la méthode initialize() du PlanningController
        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
        } else {
            System.out.println("Note : datePicker est null, ignoré pour l'instant.");
        }


        initialiserColonnes();

        chargerSemaine(currentDate);

        filterType.getItems().addAll(
                "Aucun Filtre",
                "Salle",
                "Module",
                "Enseignant"
        );

        filterType.setOnAction(e -> chargerValeursFiltre());
        filterValue.setOnAction(e -> chargerCours());

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

    private void chargerJour(LocalDate date) {

        coursTable.getItems().setAll(
                coursDAO.findByDateRange(date, date)
        );

        weekLabel.setText("Planning du " + date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.FRENCH) + " " + date);
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


        chargerJour(currentDate);
    }


    private void chargerValeursFiltre() {

        filterValue.getItems().clear();

        String type = filterType.getValue();

        if (type == null) return;

        if (type.equals("Aucun filtre")) {

            chargerSemaine(currentDate);
            return;

        }

        List<Cours> cours = coursDAO.findAll();

        if (type.equals("Salle")) {

            cours.stream()
                    .map(c -> c.getSalle() != null ? c.getSalle().getNumero() : null)
                    .filter(s -> s != null)
                    .distinct()
                    .forEach(filterValue.getItems()::add);

        }

        else if (type.equals("Module")) {

            cours.stream()
                    .map(c -> c.getModule() != null ? c.getModule().getNom() : null)
                    .filter(s -> s != null)
                    .distinct()
                    .forEach(filterValue.getItems()::add);

        }

        else if (type.equals("Enseignant")) {

            filterValue.getItems().add("Non renseigné");

        }
    }


    private void chargerCours() {

        if ("Aucun filtre".equals(filterType.getValue())) {

            chargerSemaine(currentDate);
            return;

        }

        List<Cours> cours = coursDAO.findAll();

        String type = filterType.getValue();
        String value = filterValue.getValue();

        if (type != null && value != null) {

            if (type.equals("Salle")) {

                cours = cours.stream()
                        .filter(c -> c.getSalle() != null
                                && value.equals(c.getSalle().getNumero()))
                        .toList();

            }

            else if (type.equals("Module")) {

                cours = cours.stream()
                        .filter(c -> c.getModule() != null
                                && value.equals(c.getModule().getNom()))
                        .toList();

            }

        }

        coursTable.getItems().setAll(cours);
    }

    @FXML
    private void ouvrirRechercheSalles() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SallesDispoView.fxml"));
            Parent root = loader.load();

            SallesDispoController controller = loader.getController();

            // Utilisation de JpaUtil au lieu de JpaConfig
// On utilise JpaUtil.em() car c'est le nom défini dans ta classe JpaUtil
            controller.setSalleDAO(new SalleDAO(JpaUtil.em()));

            Stage stage = new Stage();
            stage.setTitle("Recherche de Salles");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}

