package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.app.SessionUtilisateur;
import fr.univ.hyperplanning.dao.SalleDAO;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.pedagogie.Groupe;
import fr.univ.hyperplanning.model.pedagogie.SyllabusSemestre;
import fr.univ.hyperplanning.model.utilisateurs.Enseignant;
import fr.univ.hyperplanning.model.utilisateurs.Intervenant;
import fr.univ.hyperplanning.model.utilisateurs.Etudiant;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import fr.univ.hyperplanning.app.controllers.Authentification.NavigationUtil;
import fr.univ.hyperplanning.infra.config.SessionManager;
import jakarta.persistence.EntityManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import java.io.IOException;

public class MainController {

    @Getter private static MainController instance;

    @FXML private Label userNameLabel;
    @FXML private Label userInfoLabel;
    @FXML private AnchorPane contentArea;
    @FXML private VBox menuBox;

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
            } else if (role.equals("INTERVENANT")) {
                Intervenant inter = em.find(Intervenant.class, user.getId());
                if (userInfoLabel != null) userInfoLabel.setText(inter != null ? "Intervenant - " + inter.getEntreprise() : "Intervenant");
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
        } else if (role.contains("ADMIN")) {
            ajouterBoutonMenu("⚠️  Conflits", "CONFLITS", false);
            ajouterBoutonMenu("🏫  Salles", "SALLES", false);
            ajouterBoutonMenu("  Ajouter un cours", "AJOUTER_COURS", false, false);
        }

        if (!role.contains("ADMIN")) {
            ajouterBoutonMenu("👤  Mon profil", "PROFIL", false);
        }

        ajouterBoutonMenu("🚪  Déconnexion", "LOGOUT", false);
    }

    // Surcharge de la méthode pour prendre en compte la nouvelle entrée
    private void ajouterBoutonMenu(String texte, String action, boolean isActive) {
        // Elle appelle la version complète en forçant le true
        ajouterBoutonMenu(texte, action, isActive, true);
    }

    private void ajouterBoutonMenu(String texte, String action, boolean isActive, boolean isPersistent) {
        Button btn = new Button(texte);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(50);
        btn.setStyle(isActive ? STYLE_ACTIVE : STYLE_NORMAL);
        btn.setOnAction(e -> {
            if(isPersistent) { // On change l'état des boutons
                menuBox.getChildren().forEach(n -> n.setStyle(STYLE_NORMAL));
                btn.setStyle(STYLE_ACTIVE);
            }
            gererNavigation(action);
        });
        menuBox.getChildren().add(btn);
    }

    private Stage instancePopup = null;

    public void gererNavigation(String action) {
        switch (action) {
            case "DASHBOARD" -> chargerVueParRole();
            case "PLANNING" -> loadView("/fxml/planning.fxml");
            case "CONFLITS" -> loadView("/fxml/ConflictView.fxml");
            case "SALLES" -> loadViewSalles();
            case "LOGOUT" -> seDeconnecter();
            case "AJOUTER_COURS" -> ouvrirPopUp("/fxml/ajouterCours.fxml", 600, 500);
        }
    }

    /**
     * Méthode pour ouvrir un popup
     * @Param path
     * Chemin de la page FXML
     * @Param v
     * Largeur du popup
     * @Param v1
     * Hauteur du popup
     *
     * On force l'utilisation de setMinWitdh et setMinHeight car sinon à cause de certains FXML
     * comme les combo type le popup s'ouvre en tout petit.
     */
    private void ouvrirPopUp(String path, int v, int v1) {
        // Si la fenêtre est déjà ouverte et visible, on la met juste au premier plan
        if (instancePopup != null && instancePopup.isShowing()) {
            instancePopup.toFront();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            instancePopup = new Stage();
            instancePopup.setScene(new Scene(root, v, v1));
            instancePopup.setTitle("Ajouter un cours");
            instancePopup.setMinWidth(v);
            instancePopup.setMinHeight(v1);
            instancePopup.centerOnScreen();
            instancePopup.initModality(Modality.NONE);
            instancePopup.show();
        } catch (IOException e) {
            e.printStackTrace();
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

    private void loadViewSalles() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SallesDispoView.fxml"));
            Parent view = loader.load();
            SallesDispoController ctrl = loader.getController();
            ctrl.setSalleDAO(new SalleDAO()); // Utilisation du constructeur vide
            contentArea.getChildren().setAll(view);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void chargerVueParRole() {
        Utilisateur user = SessionUtilisateur.get();
        if (user == null) return;

        String role = user.getRole().name();

        // Navigation directe vers les fichiers correspondants
        if (role.equals("ETUDIANT")) {
            loadView("/fxml/dashboard_etudiant.fxml");
        } else if (role.equals("ENSEIGNANT")) {
            loadView("/fxml/dashboard_enseignant.fxml");
        } else if (role.equals("INTERVENANT")) {
            loadView("/fxml/dashboard_intervenant.fxml");
        } else {
            loadView("/fxml/dashboard_admin.fxml");
        }
    }
}