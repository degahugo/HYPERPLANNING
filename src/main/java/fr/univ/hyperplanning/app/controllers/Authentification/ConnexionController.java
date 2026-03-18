package fr.univ.hyperplanning.app.controllers.Authentification;

import fr.univ.hyperplanning.infra.config.AuthService;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ConnexionController {

    @FXML private TextField     fieldUsername;
    @FXML private PasswordField fieldPassword;
    @FXML private Label         labelErreur;
    @FXML private Button        btnConnexion;
    @FXML private TextField     champMotDePasseVisible;
    @FXML private Button        btnVisibiliteMotDePasse;
    @FXML private ImageView     iconeVisibilite;

    private boolean estMotDePasseVisible = false;
    private final Image iconeOuvert  = new Image(getClass().getResourceAsStream("/images/eye_open.png"));
    private final Image iconeFerme   = new Image(getClass().getResourceAsStream("/images/eye_close.png"));


    @FXML
    private void basculerVisibiliteMotDePasse() {
        estMotDePasseVisible = !estMotDePasseVisible;

        if (estMotDePasseVisible) {
            champMotDePasseVisible.setText(fieldPassword.getText());
            champMotDePasseVisible.setVisible(true);
            champMotDePasseVisible.setManaged(true);
            fieldPassword.setVisible(false);
            fieldPassword.setManaged(false);
            iconeVisibilite.setImage(iconeOuvert);
        } else {
            fieldPassword.setText(champMotDePasseVisible.getText());
            fieldPassword.setVisible(true);
            fieldPassword.setManaged(true);
            champMotDePasseVisible.setVisible(false);
            champMotDePasseVisible.setManaged(false);
            iconeVisibilite.setImage(iconeFerme);
        }
    }

    private String recupererMotDePasse() {
        return estMotDePasseVisible ? champMotDePasseVisible.getText() : fieldPassword.getText();
    }
    

    @FXML
    private void seConnecter() {
        labelErreur.setText("");

        String username = fieldUsername.getText().trim();
        String password = recupererMotDePasse();

        // Validation côté UI
        if (username.isEmpty() || password.isEmpty()) {
            labelErreur.setText("Veuillez remplir tous les champs.");
            return;
        }

        // Appel au service
        Utilisateur utilisateur = AuthService.connecter(username, password);

        if (utilisateur == null) {
            labelErreur.setText("Identifiants incorrects. Vérifiez votre nom d'utilisateur et mot de passe.");
            return;
        }
        //Vérifier que l'on affiche bien le role
        System.out.println("Connecté : id=" + utilisateur.getId() + " | role=" + utilisateur.getRole());
        // Connexion réussie -> redirection vers le tableau de bord principal
        NavigationUtil.naviguerVers("/fxml/main.fxml", btnConnexion);
    }
    
    @FXML private void ouvrirReinitialisationEmail() {
        NavigationUtil.naviguerVers("/fxml/reinitialisation_email.fxml", btnConnexion);
    }

    @FXML private void ouvrirInscription() {
        NavigationUtil.naviguerVers("/fxml/inscription.fxml", btnConnexion);
    }

    @FXML private void ouvrirMentions()      { System.out.println("Mentions légales"); }
    @FXML private void ouvrirGuide()         { System.out.println("Guide d'utilisation"); }
}
