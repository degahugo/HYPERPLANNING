package fr.univ.hyperplanning.app.controllers.Authentification;

import fr.univ.hyperplanning.dao.UtilisateurDAO;
import fr.univ.hyperplanning.infra.config.AuthService;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class InscriptionController {

    @FXML private TextField     fieldNom;
    @FXML private TextField     fieldPrenom;
    @FXML private TextField     fieldEmail;
    @FXML private PasswordField fieldPassword;
    @FXML private PasswordField fieldConfirmPassword;
    @FXML private Label         labelErreur;
    @FXML private Button        btnValider;

    
    @FXML
    private void validerInscription() {
        labelErreur.setText("");

        String nom     = fieldNom.getText().trim();
        String prenom  = fieldPrenom.getText().trim();
        String email   = fieldEmail.getText().trim();
        String password = fieldPassword.getText();
        String confirm  = fieldConfirmPassword.getText();

        // Validation champs vides
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirm.isEmpty()) {
            labelErreur.setText("Veuillez remplir tous les champs.");
            return;
        }

        // Confirmation mot de passe
        if (!password.equals(confirm)) {
            labelErreur.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        try {
            Utilisateur nouvelUtilisateur = AuthService.inscrire(nom, prenom, email, password);

            // Afficher le nom d'utilisateur généré à l'utilisateur
            String username = UtilisateurDAO.buildUsername(nouvelUtilisateur);
            labelErreur.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px;");
            labelErreur.setText("Compte créé ! Votre identifiant : " + username);

            // Redirection vers la connexion après un court délai
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() ->
                        NavigationUtil.naviguerVers("/fxml/connexion.fxml", btnValider));
            }).start();

        } catch (IllegalArgumentException ex) {
            labelErreur.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 12px;");
            labelErreur.setText(ex.getMessage());
        } catch (Exception ex) {
            labelErreur.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 12px;");
            labelErreur.setText("Erreur lors de la création du compte. Réessayez.");
            ex.printStackTrace();
        }
    }

    
    @FXML private void ouvrirConnexion() {
        NavigationUtil.naviguerVers("/fxml/connexion.fxml", btnValider);
    }

    @FXML private void ouvrirMentions() { System.out.println("Mentions légales"); }
    @FXML private void ouvrirGuide()    { System.out.println("Guide d'utilisation"); }
}
