package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.app.controllers.Authentification.NavigationUtil;
import fr.univ.hyperplanning.dao.CoursDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class GestionCoursController {

    private final CoursDAO coursDAO = new CoursDAO();

    @FXML
    private Button btnRetour;

    public void onBtnRetour() {
        NavigationUtil.naviguerVers("/fxml/planning.fxml", btnRetour);
    }

}
