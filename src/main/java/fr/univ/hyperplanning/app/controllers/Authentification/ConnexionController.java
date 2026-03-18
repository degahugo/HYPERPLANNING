package fr.univ.hyperplanning.app.controllers.Authentification;
import fr.univ.hyperplanning.app.SessionUtilisateur;
import fr.univ.hyperplanning.infra.config.AuthService;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ConnexionController {

    @FXML private TextField     fieldUsername;
    @FXML private PasswordField fieldPassword;
    @FXML private Label         labelErreur;
    @FXML private Button        btnConnexion;


    @FXML
    private void seConnecter() {
        labelErreur.setText("");

        String username = fieldUsername.getText().trim();
        String password = fieldPassword.getText();          // pas de trim sur le mdp

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

        SessionUtilisateur.set(utilisateur);

        // Connexion réussie → redirection vers le tableau de bord principal
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
