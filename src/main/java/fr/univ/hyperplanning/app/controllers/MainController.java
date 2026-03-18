package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.app.SessionUtilisateur;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML
    private AnchorPane contentArea;

    @FXML
    private VBox menuBox;

    @FXML
    public void initialize() {
        initMenu();
        chargerVueParRole();
    }

    // CHARGEMENT DES VUES
    public void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));

            if (loader.getLocation() == null) {
                System.out.println("❌ FXML introuvable : " + fxml);
                return;
            }

            Parent view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    // MENU DYNAMIQUE

    private void initMenu() {

        menuBox.getChildren().clear();

        Utilisateur user = SessionUtilisateur.get();
        if (user == null) return;

        String role = user.getRole().name();

        // COMMUN
        Button btnPlanning = new Button("📅 Emploi du temps");
        btnPlanning.setOnAction(e -> loadView("/fxml/planning.fxml"));
        menuBox.getChildren().add(btnPlanning);

        // ETUDIANT
        if (role.equals("ETUDIANT")) {

            Button btnCours = new Button("📚 Cours");
            btnCours.setOnAction(e -> loadView("/fxml/cours.fxml"));

            Button btnExamens = new Button("📝 Examens");
            btnExamens.setOnAction(e -> loadView("/fxml/examens.fxml"));

            menuBox.getChildren().addAll(btnCours, btnExamens);
        }

        // ENSEIGNANT
        else if (role.equals("ENSEIGNANT")) {

            Button btnAccueil = new Button("🏠 Accueil");
            btnAccueil.setOnAction(e -> loadView("/fxml/accueil.fxml"));

            Button btnDemandes = new Button("📨 Mes demandes");
            btnDemandes.setOnAction(e -> loadView("/fxml/demandes.fxml"));

            menuBox.getChildren().addAll(btnAccueil, btnDemandes);
        }

        //  ADMIN
        else if (role.equals("ADMINISTRATION")) {

            Button btnGestionCours = new Button("⚙️ Gérer cours");
            btnGestionCours.setOnAction(e -> loadView("/fxml/gestion_cours.fxml"));

            Button btnConflits = new Button("⚠️ Gérer conflits");
            btnConflits.setOnAction(e -> loadView("/fxml/ConflictView.fxml"));

            Button btnDemandes = new Button("📨 Gérer demandes");
            btnDemandes.setOnAction(e -> loadView("/fxml/demandes.fxml"));

            Button btnSalles = new Button("🏫 Salles disponibles");
            btnSalles.setOnAction(e -> loadView("/fxml/SallesDispoView.fxml"));

            menuBox.getChildren().addAll(
                    btnGestionCours,
                    btnConflits,
                    btnDemandes,
                    btnSalles
            );
        }
    }


    // VUE PAR DEFAUT

    private void chargerVueParRole() {

        Utilisateur user = SessionUtilisateur.get();
        if (user == null) return;


        loadView("/fxml/planning.fxml");
    }
}