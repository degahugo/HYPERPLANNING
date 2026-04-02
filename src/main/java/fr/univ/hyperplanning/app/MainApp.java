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

    /* s'exécute sur un Thread séparé avant l'affichage : idéal pour seed()
    avant que l'utilisateur intéragisse.
    */

    @Override
    public void init() {
        try {
            DataInit.seed();
        } catch (Exception e) {
            System.err.println("[MainApp] Erreur durant le seed : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*
    Exécuté sur le Thread JavaFX, ne fait que l'affichage
    bulk() est lancé en thread daemon, la fenètre s'affiche immédiatement
    les données s'insèrent en arrière plan
     */

    @Override
    public void start(Stage stage) throws Exception {
        Thread bulkThread = new Thread(() -> {
            try {
                DataInit.bulk(500);
            } catch (Exception e) {
                System.err.println("[MainApp] Erreur durant le bulk : " + e.getMessage());
                e.printStackTrace();
            }
        }, "bulk-thread");
        bulkThread.setDaemon(true);
        bulkThread.start();

        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/accueil.fxml"));
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