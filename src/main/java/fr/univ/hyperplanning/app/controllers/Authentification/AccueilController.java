package fr.univ.hyperplanning.app.controllers.Authentification;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class AccueilController {

    @FXML private Button btnConnexion;
    @FXML private Button btnInscription;

    @FXML
    private void ouvrirConnexion() {
        NavigationUtil.naviguerVers("/fxml/connexion.fxml", btnConnexion);
    }

    @FXML
    private void ouvrirInscription() {
        NavigationUtil.naviguerVers("/fxml/inscription.fxml", btnInscription);
    }

    @FXML
    private void ouvrirMentions() {
        System.out.println("Mentions légales");
    }

    @FXML
    private void ouvrirGuide() {
        System.out.println("Guide d'utilisation");
    }
}