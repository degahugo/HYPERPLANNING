package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.dao.SalleDAO;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

//Ajout bouton pour se deconnecter
import fr.univ.hyperplanning.app.controllers.Authentification.NavigationUtil;
import fr.univ.hyperplanning.infra.config.SessionManager;
import javafx.scene.control.Button;


public class MainController {

    @FXML
    private Button btnDeconnexion;

    @FXML
    private void seDeconnecter() {
        SessionManager.fermer();
        NavigationUtil.naviguerVers("/fxml/accueil.fxml", btnDeconnexion);
    }

    @FXML
    private void ouvrirRechercheSalles() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SallesDispoView.fxml"));
            Parent root = loader.load();

            SallesDispoController controller = loader.getController();
            // On utilise bien JpaUtil.em() comme on l'a vu tout à l'heure
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