package fr.univ.hyperplanning.app.controllers.Authentification;

import fr.univ.hyperplanning.infra.config.AuthService;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class Reinitialisation_passwordController {

    @FXML private PasswordField fieldPassword;
    @FXML private PasswordField fieldConfirmPassword;
    @FXML private Label         labelErreur;
    @FXML private Button        btnConfirmer;

    
    @FXML
    private void confirmerReinitialisation() {
        labelErreur.setText("");

        String password = fieldPassword.getText();
        String confirm  = fieldConfirmPassword.getText();

        if (password.isEmpty() || confirm.isEmpty()) {
            labelErreur.setText("Veuillez remplir tous les champs.");
            return;
        }

        if (!password.equals(confirm)) {
            labelErreur.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        Utilisateur utilisateur = ReinitialisationContext.getUtilisateur();
        if (utilisateur == null) {
            // Sécurité : si on arrive ici sans contexte, on renvoie à l'étape 1
            labelErreur.setText("Session expirée. Veuillez recommencer.");
            NavigationUtil.naviguerVers("/fxml/reinitialisation_email.fxml", btnConfirmer);
            return;
        }

        try {
            AuthService.reinitialiserMotDePasse(utilisateur, password);
            ReinitialisationContext.clear();

            labelErreur.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px;");
            labelErreur.setText("Mot de passe modifié avec succès ! Redirection…");

            // Redirection vers connexion après 2 s
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() ->
                        NavigationUtil.naviguerVers("/fxml/connexion.fxml", btnConfirmer));
            }).start();

        } catch (IllegalArgumentException ex) {
            labelErreur.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 12px;");
            labelErreur.setText(ex.getMessage());
        } catch (Exception ex) {
            labelErreur.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 12px;");
            labelErreur.setText("Erreur lors de la mise à jour. Réessayez.");
            ex.printStackTrace();
        }
    }

    
    @FXML private void ouvrirInscription() {
        NavigationUtil.naviguerVers("/fxml/inscription.fxml", btnConfirmer);
    }

    @FXML private void ouvrirMentions() { System.out.println("Mentions légales"); }
    @FXML private void ouvrirGuide()    { System.out.println("Guide d'utilisation"); }
}
