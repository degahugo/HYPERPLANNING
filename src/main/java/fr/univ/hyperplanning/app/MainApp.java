package fr.univ.hyperplanning.app;

import fr.univ.hyperplanning.infra.config.AuthService;
import fr.univ.hyperplanning.infra.config.DataInit;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/accueil.fxml"));
        DataInit.seed();
        Parent root = loader.load(); //  plus d'erreur Object -> Parent
        stage.setTitle("Hyperplanning");
        stage.setScene(new Scene(root, 900, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        AuthService.deconnecter();
        JpaUtil.close();
    }
}