package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.dao.CoursDAO;
import fr.univ.hyperplanning.dao.ModuleDAO;
import fr.univ.hyperplanning.dao.SalleDAO;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.enums.TypeCours;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.pedagogie.ModuleCours;
import fr.univ.hyperplanning.model.pedagogie.Salle;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class AjouterCoursController {

    @FXML private TextField txtIntitule, txtHeureDebut, txtHeureFin;
    @FXML private DatePicker dateDebutPicker;
    @FXML private ComboBox<TypeCours> comboType;
    @FXML private ComboBox<ModuleCours> comboModule;
    @FXML private ComboBox<Salle> comboSalle;
    @FXML private CheckBox checkCC;

    private final CoursDAO coursDAO = new CoursDAO();
    private final SalleDAO salleDAO = new SalleDAO(JpaUtil.em());
    private final ModuleDAO moduleDAO = new ModuleDAO();

    @FXML
    public void initialize() {
        // Remplir les types (Enum)
        comboType.getItems().setAll(TypeCours.values());

        // Remplir les modules et salles (depuis la BDD)
        comboSalle.getItems().setAll(salleDAO.findAll());
        comboModule.getItems().setAll(moduleDAO.findAll());
    }

    @FXML
    private void onEnregistrer() {
        try {
            System.out.println("ENREGISTRER COURS BD");
            // 1. Validation basique
            if (txtIntitule.getText().isEmpty() || comboModule.getValue() == null ||
                    comboSalle.getValue() == null || dateDebutPicker.getValue() == null) {
                System.err.println("Veuillez remplir les champs obligatoires.");
                // Ajouter un popup qui ne permet pas de cliquer sur les autres fenêtre de l'application en le forcant à lire le message Veuillez remplir les champs obligatoires.
                return;
            }

            // 2. Construction de l'objet Cours
            Cours cours = new Cours();
            cours.setIntitule(txtIntitule.getText());
            cours.setModule(comboModule.getValue());
            cours.setSalle(comboSalle.getValue());
            // Retirer la checkbox et mettre setEstCC enfonction de la valeur de comboType
            TypeCours typeCours = comboType.getValue();
            cours.setTypeCours(typeCours);
            cours.setEstCC(typeCours == TypeCours.CC);
            cours.setEstAnnule(false); // Par défaut à la création
            cours.setAnnotation("");

            // Gestion des dates
            LocalDateTime debut = dateDebutPicker.getValue().atTime(LocalTime.parse(txtHeureDebut.getText()));
            LocalDateTime fin = dateDebutPicker.getValue().atTime(LocalTime.parse(txtHeureFin.getText()));

            cours.setDateDebut(debut);
            cours.setDateFin(fin);

            // Calcul de la durée en minutes
            long minutes = java.time.Duration.between(debut, fin).toMinutes();
            cours.setDuree((int) minutes);

            // Vérifier conflit avant d'ajouter
            coursDAO.nouveauCours(cours);

            // Fermer popup
            onAnnuler();

        } catch (Exception e) {
            e.printStackTrace();
            // Optionnel : Afficher une alerte JavaFX ici
        }
    }

    @FXML
    private void onAnnuler() {
        // Récupère le Stage actuel pour le fermer
        ((Stage) txtIntitule.getScene().getWindow()).close();
    }
}