package fr.univ.hyperplanning.app.controllers.Authentification;

import fr.univ.hyperplanning.infra.config.AuthService;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class Reinitialisation_emailController {

    @FXML private TextField fieldEmail;
    @FXML private TextField fieldUsername;
    @FXML private Label     labelErreur;
    @FXML private Button    btnConfirmer;

    
    @FXML
    private void confirmer() {
        labelErreur.setText("");

        String email    = fieldEmail.getText().trim();
        String username = fieldUsername.getText().trim();

        if (email.isEmpty() || username.isEmpty()) {
            labelErreur.setText("Veuillez remplir tous les champs.");
            return;
        }

        Utilisateur utilisateur = AuthService.verifierIdentiteReinitialisation(email, username);

        if (utilisateur == null) {
            labelErreur.setText("Aucun compte ne correspond à ces informations.");
            return;
        }

        // On passe l'utilisateur à l'écran suivant via le contexte statique
        ReinitialisationContext.setUtilisateur(utilisateur);
        NavigationUtil.naviguerVers("/fxml/reinitialisation_password.fxml", btnConfirmer);
    }

    
    @FXML private void ouvrirInscription() {
        NavigationUtil.naviguerVers("/fxml/inscription.fxml", btnConfirmer);
    }

    @FXML private void ouvrirMentions() { System.out.println("Mentions légales"); }
    @FXML private void ouvrirGuide()    { System.out.println("Guide d'utilisation"); }
}
