package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.app.SessionUtilisateur;
import fr.univ.hyperplanning.dao.SalleDAO;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.pedagogie.Groupe;
import fr.univ.hyperplanning.model.pedagogie.SyllabusSemestre;
import fr.univ.hyperplanning.model.utilisateurs.Enseignant;
import fr.univ.hyperplanning.model.utilisateurs.Etudiant;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import fr.univ.hyperplanning.app.controllers.Authentification.NavigationUtil;
import fr.univ.hyperplanning.infra.config.SessionManager;
import jakarta.persistence.EntityManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
<<<<<<< HEAD

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

//Ajout bouton pour se deconnecter
import fr.univ.hyperplanning.app.controllers.Authentification.NavigationUtil;
import fr.univ.hyperplanning.infra.config.SessionManager;
import javafx.scene.control.Button;

=======
import lombok.Getter;
import java.io.IOException;
>>>>>>> work

public class MainController {

    @Getter private static MainController instance;

    @FXML private Label userNameLabel;
    @FXML private Label userInfoLabel;
    @FXML private AnchorPane contentArea;
    @FXML private VBox menuBox;
    @FXML private Button btnDeconnexion;

    private final String STYLE_NORMAL = "-fx-background-color: transparent; -fx-text-fill: #4B5563; -fx-font-size: 15; -fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 20; -fx-cursor: hand;";
    private final String STYLE_ACTIVE = "-fx-background-color: #EFF6FF; -fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-background-radius: 12; -fx-font-size: 15; -fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 20; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        instance = this;
        Utilisateur user = SessionUtilisateur.get();
        if (user != null) {
            if (userNameLabel != null) userNameLabel.setText(user.getNom() + " " + user.getPrenom());
            configurerProfilBas(user);
            initMenu(user.getRole().name());
            chargerVueParRole();
        }
    }

    @FXML
    private void seDeconnecter() {
        SessionManager.fermer();
        NavigationUtil.naviguerVers("/fxml/connexion.fxml", menuBox);
    }

    private void configurerProfilBas(Utilisateur user) {
        String role = user.getRole().name();
        EntityManager em = JpaUtil.em();
        try {
            if (role.equals("ETUDIANT")) {
                Etudiant etu = em.find(Etudiant.class, user.getId());
                if (etu != null && !etu.getGroupes().isEmpty()) {
                    Groupe g = etu.getGroupes().iterator().next();
                    SyllabusSemestre s = g.getSyllabus();
                    if (userInfoLabel != null) userInfoLabel.setText((s != null ? "S" + s.getSemestre() : "") + " - " + g.getLibelle());
                }
            } else if (role.equals("ENSEIGNANT")) {
                Enseignant ens = em.find(Enseignant.class, user.getId());
                if (userInfoLabel != null) userInfoLabel.setText(ens != null ? ens.getSpecialite() : "Enseignant");
            } else {
                if (userInfoLabel != null) userInfoLabel.setText("Administration");
            }
        } finally { em.close(); }
    }

    private void initMenu(String role) {
        menuBox.getChildren().clear();
        ajouterBoutonMenu("⊞  Tableau de bord", "DASHBOARD", true);
        ajouterBoutonMenu("📅  Emploi du temps", "PLANNING", false);

        if (role.equals("ETUDIANT")) {
            ajouterBoutonMenu("📖  Devoirs", "DEVOIRS", false);
            ajouterBoutonMenu("⫶三  Examens", "EXAMENS", false);
        } else if (role.equals("ADMINISTRATION") || role.equals("ADMIN")) {
            ajouterBoutonMenu("⚠️  Conflits", "CONFLITS", false);
            ajouterBoutonMenu("🏫  Salles", "SALLES", false);
        }

        ajouterBoutonMenu("🚪  Déconnexion", "LOGOUT", false);
    }

    private void ajouterBoutonMenu(String texte, String action, boolean isActive) {
        Button btn = new Button(texte);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(50);
        btn.setStyle(isActive ? STYLE_ACTIVE : STYLE_NORMAL);
        btn.setOnAction(e -> {
            menuBox.getChildren().forEach(n -> n.setStyle(STYLE_NORMAL));
            btn.setStyle(STYLE_ACTIVE);
            gererNavigation(action);
        });
        menuBox.getChildren().add(btn);
    }

    public void gererNavigation(String action) {
        switch (action) {
            case "DASHBOARD" -> chargerVueParRole();
            case "PLANNING" -> loadView("/fxml/planning.fxml");
            case "CONFLITS" -> loadView("/fxml/ConflictView.fxml");
            case "SALLES" -> loadViewSalles();
            case "LOGOUT" -> seDeconnecter();
        }
    }

    public void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0); AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0); AnchorPane.setRightAnchor(view, 0.0);
        } catch (IOException e) { e.printStackTrace(); }
    }

<<<<<<< HEAD

    @FXML
    private void ouvrirRechercheSalles(ActionEvent event) {
        System.out.println("Bouton recherche cliqué !");
        // Ajoute ici le code pour ouvrir la vue des salles
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
=======
    private void loadViewSalles() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SallesDispoView.fxml"));
            Parent view = loader.load();
            SallesDispoController ctrl = loader.getController();
            ctrl.setSalleDAO(new SalleDAO(JpaUtil.em()));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) { e.printStackTrace(); }
>>>>>>> work
    }

    private void chargerVueParRole() {
        String role = SessionUtilisateur.get().getRole().name();
        if (role.equals("ETUDIANT")) loadView("/fxml/dashboard_etudiant.fxml");
        else if (role.equals("ENSEIGNANT")) loadView("/fxml/dashboard_enseignant.fxml");
        else loadView("/fxml/dashboard_admin.fxml");
    }
}