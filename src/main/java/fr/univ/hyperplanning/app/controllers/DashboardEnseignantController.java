package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.app.SessionUtilisateur;
import fr.univ.hyperplanning.dao.CoursDAO;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.utilisateurs.Intervenant;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.util.List;

public class DashboardEnseignantController {

    @FXML private Label totalCoursLabel;
    @FXML private Label demandesAttenteLabel;

    private final CoursDAO coursDAO = new CoursDAO();

    @FXML
    public void initialize() {
        Utilisateur user = SessionUtilisateur.get();
        if (user != null) {
            LocalDate debut = LocalDate.of(2026, 1, 1);
            LocalDate fin = LocalDate.of(2026, 7, 1);

            List<Cours> mesCours ;
            if (user instanceof Intervenant) {
                mesCours = coursDAO.findByIntervenant(user.getId(), debut, fin);
            } else {
                mesCours = coursDAO.findByEnseignant(user.getId(), debut, fin);
            }


            totalCoursLabel.setText(String.valueOf(mesCours.size()));
        }
        demandesAttenteLabel.setText("3");

    }

    @FXML
    private void handleVoirToutPlanning() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().gererNavigation("PLANNING");
        }
    }

    @FXML
    private void ouvrirRechercheSalles() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().gererNavigation("SALLES");
        }
    }
}