package fr.univ.hyperplanning.app.controllers.Authentification;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationUtil {

    public static void naviguerVers(String fxmlPath, javafx.scene.Node source) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) source.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("Erreur navigation vers " + fxmlPath + " : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
