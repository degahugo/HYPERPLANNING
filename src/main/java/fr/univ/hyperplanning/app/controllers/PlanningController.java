package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.app.SessionUtilisateur;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import fr.univ.hyperplanning.model.utilisateurs.Etudiant;
import fr.univ.hyperplanning.dao.CoursDAO;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.dao.SalleDAO;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import jakarta.persistence.EntityManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class PlanningController {

    private final CoursDAO coursDAO = new CoursDAO();

    @FXML private TableView<Cours> coursTable;
    @FXML private TableColumn<Cours, String> colJour;
    @FXML private TableColumn<Cours, String> colHeureDebut;
    @FXML private TableColumn<Cours, String> colHeureFin;
    @FXML private TableColumn<Cours, String> colDuree;
    @FXML private TableColumn<Cours, String> colModule;
    @FXML private TableColumn<Cours, String> colSalle;
    @FXML private TableColumn<Cours, String> colEnseignant;

    @FXML private DatePicker datePicker;
    @FXML private Label weekLabel;

    @FXML private ComboBox<String> filterType;
    @FXML private ComboBox<String> filterValue;

    private LocalDate currentDate;

    // ==========================
    // INITIALISATION
    // ==========================
    @FXML
    public void initialize() {

        System.out.println("PlanningController chargé");

        currentDate = LocalDate.now();

        if (datePicker != null) {
            datePicker.setValue(currentDate);
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
                    afficherDetailsCours(row.getItem());
                }
            });

            return row;
        });
    }

    // ==========================
    // COLONNES
    // ==========================
    private void initialiserColonnes() {

        colJour.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getDateDebut()
                                .toLocalDate()
                                .getDayOfWeek()
                                .getDisplayName(java.time.format.TextStyle.FULL, Locale.FRENCH)
                )
        );

        colHeureDebut.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDateDebut().toString())
        );

        colHeureFin.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDateFin().toString())
        );

        colDuree.setCellValueFactory(cell -> {
            int minutes = cell.getValue().getDuree();
            int heures = minutes / 60;
            int reste = minutes % 60;
            return new SimpleStringProperty(heures + "h" + (reste > 0 ? reste : ""));
        });

        colModule.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getModule().getNom())
        );

        colSalle.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getSalle().getNumero())
        );

        colEnseignant.setCellValueFactory(cell ->
                new SimpleStringProperty("Non renseigné")
        );
    }

    // ==========================
    // CHARGER ETUDIANT AVEC GROUPES
    // ==========================
    private Etudiant chargerEtudiantAvecGroupes(Long id) {

        EntityManager em = JpaUtil.em();

        Etudiant etu = em.createQuery(
                        "SELECT e FROM Etudiant e LEFT JOIN FETCH e.groupes WHERE e.id = :id",
                        Etudiant.class
                )
                .setParameter("id", id)
                .getSingleResult();

        em.close();

        return etu;
    }

    // ==========================
    // CHARGER SEMAINE (UC PRINCIPAL)
    // ==========================
    private void chargerSemaine(LocalDate date) {

        LocalDate debut = date.with(DayOfWeek.MONDAY);
        LocalDate fin = date.with(DayOfWeek.SUNDAY);

        weekLabel.setText("Semaine : " + debut + " → " + fin);

        Utilisateur user = SessionUtilisateur.get();

        List<Cours> tous = coursDAO.findByDateRange(debut, fin);

        if (tous.isEmpty()) {
            coursTable.getItems().clear();
            System.out.println("⚠ Aucun cours pour cette semaine");
            return;
        }

        if (user == null) {
            coursTable.getItems().setAll(tous);
            return;
        }

        String role = user.getRole().name();

        if (role.equals("ETUDIANT")) {

            Etudiant etu = chargerEtudiantAvecGroupes(user.getId());

            List<Cours> filtres = tous.stream()
                    .filter(cours -> cours.getGroupes().stream()
                            .anyMatch(g -> etu.getGroupes().stream()
                                    .anyMatch(ge -> ge.getId().equals(g.getId()))
                            ))
                    .toList();

            coursTable.getItems().setAll(filtres);
        }

        else {
            coursTable.getItems().setAll(tous);
        }
    }

    // ==========================
    // JOUR
    // ==========================
    private void chargerJour(LocalDate date) {
        coursTable.getItems().setAll(
                coursDAO.findByDateRange(date, date)
        );

        weekLabel.setText("Planning du " + date);
    }

    // ==========================
    // NAVIGATION
    // ==========================
    @FXML
    private void onWeekView() {
        currentDate = datePicker.getValue();
        chargerSemaine(currentDate);
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

    // ==========================
    // FILTRES
    // ==========================
    private void chargerValeursFiltre() {

        filterValue.getItems().clear();
        String type = filterType.getValue();

        if (type == null || type.equals("Aucun Filtre")) {
            chargerSemaine(currentDate);
            return;
        }

        List<Cours> cours = coursDAO.findAll();

        if (type.equals("Salle")) {
            cours.stream()
                    .map(c -> c.getSalle().getNumero())
                    .distinct()
                    .forEach(filterValue.getItems()::add);
        }

        else if (type.equals("Module")) {
            cours.stream()
                    .map(c -> c.getModule().getNom())
                    .distinct()
                    .forEach(filterValue.getItems()::add);
        }

        else if (type.equals("Enseignant")) {
            filterValue.getItems().add("Non renseigné");
        }
    }

    private void chargerCours() {

        if ("Aucun Filtre".equals(filterType.getValue())) {
            chargerSemaine(currentDate);
            return;
        }

        List<Cours> cours = coursDAO.findAll();

        String type = filterType.getValue();
        String value = filterValue.getValue();

        if (type.equals("Salle")) {
            cours = cours.stream()
                    .filter(c -> c.getSalle().getNumero().equals(value))
                    .toList();
        }

        else if (type.equals("Module")) {
            cours = cours.stream()
                    .filter(c -> c.getModule().getNom().equals(value))
                    .toList();
        }

        coursTable.getItems().setAll(cours);
    }

    // ==========================
    // POPUP DETAILS
    // ==========================
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
}