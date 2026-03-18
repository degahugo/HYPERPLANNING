package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.app.SessionUtilisateur;
import fr.univ.hyperplanning.dao.SalleDAO;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML
    private AnchorPane contentArea;

    @FXML
    private VBox menuBox;

    @FXML
    public void initialize() {
        initMenu();
        chargerVueParRole(); // 🔥 IMPORTANT
    }


    public void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // MENU DYNAMIQUE

    private void initMenu() {

        menuBox.getChildren().clear();

        Utilisateur user = SessionUtilisateur.get();

        if (user == null) {
            System.out.println("⚠ Aucun utilisateur connecté");
            return;
        }

        String role = user.getRole().name();

        // MENU COMMUN
        Button btnPlanning = new Button("📅 Emploi du temps");
        btnPlanning.setOnAction(e -> loadView("/fxml/planning.fxml"));

        menuBox.getChildren().add(btnPlanning);

        //  ETUDIANT
        if (role.equals("ETUDIANT")) {

            Button btnCours = new Button("📚 Cours");
            Button btnExamens = new Button("📝 Examens");

            btnCours.setOnAction(e -> loadView("/fxml/cours.fxml"));
            btnExamens.setOnAction(e -> loadView("/fxml/examens.fxml"));

            menuBox.getChildren().addAll(btnCours, btnExamens);
        }

        //  ENSEIGNANT
        else if (role.equals("ENSEIGNANT")) {

            Button btnDemandes = new Button("📨 Mes demandes");
            btnDemandes.setOnAction(e -> loadView("/fxml/demandes.fxml"));

            menuBox.getChildren().add(btnDemandes);
        }

        // ADMIN
        else if (role.equals("ADMINISTRATION")) {

            Button btnGestionCours = new Button("⚙️ Gérer cours");
            Button btnConflits = new Button("⚠️ Conflits");

            btnGestionCours.setOnAction(e -> loadView("/fxml/gestion_cours.fxml"));
            btnConflits.setOnAction(e -> loadView("/fxml/conflits.fxml"));

            menuBox.getChildren().addAll(btnGestionCours, btnConflits);
        }
    }


    // UC PRINCIPAL

    private void chargerVueParRole() {

        Utilisateur user = SessionUtilisateur.get();

        if (user == null) return;

        String role = user.getRole().name();

        if (role.equals("ETUDIANT")) {
            loadView("/fxml/planning.fxml");
        }

        else if (role.equals("ENSEIGNANT")) {
            loadView("/fxml/planning_enseignant.fxml");
        }

        else if (role.equals("ADMINISTRATION")) {
            loadView("/fxml/admin_dashboard.fxml");
        }
    }


    //  SALLES

    @FXML
    private void ouvrirRechercheSalles() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SallesDispoView.fxml"));
            Parent root = loader.load();

            SallesDispoController controller = loader.getController();
            controller.setSalleDAO(new SalleDAO(JpaUtil.em()));

            Stage stage = new Stage();
            stage.setTitle("Recherche de Salles Disponibles");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}