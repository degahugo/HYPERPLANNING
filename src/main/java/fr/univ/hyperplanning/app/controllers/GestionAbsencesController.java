package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.dao.AbsenceDAO;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.infra.config.SessionManager;
import fr.univ.hyperplanning.model.enums.StatutAbsence;
import fr.univ.hyperplanning.model.gestion.Absence;
import fr.univ.hyperplanning.model.utilisateurs.Administration;
import jakarta.persistence.EntityManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.awt.Desktop;
import java.io.File;
import java.time.LocalDateTime;

public class GestionAbsencesController {

    @FXML private TableView<Absence> tableAbsences;
    @FXML private TableColumn<Absence, String> colEtudiant, colCours, colDate, colMotif;
    @FXML private TableColumn<Absence, Void> colJustificatifs, colActions;

    private final AbsenceDAO absenceDAO = new AbsenceDAO();

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerDonnees();
    }

    private void configurerColonnes() {
        colEtudiant.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEtudiantAbsent().getNom() + " " + data.getValue().getEtudiantAbsent().getPrenom()));

        colCours.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCours().getIntitule()));

        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateAbsence().toString()));

        colMotif.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMotif()));

        // Colonne pour ouvrir les fichiers
        colJustificatifs.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("📁 Voir (" + 0 + ")");
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Absence abs = getTableView().getItems().get(getIndex());
                    btnVoir.setText("📁 Voir (" + abs.getPiecesJointesPaths().size() + ")");
                    btnVoir.setOnAction(e -> ouvrirFichiers(abs));
                    setGraphic(btnVoir);
                }
            }
        });

        // Colonne pour Valider / Refuser
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnValider = new Button("✅");
            private final Button btnRefuser = new Button("❌");
            private final HBox container = new HBox(10, btnValider, btnRefuser);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Absence abs = getTableView().getItems().get(getIndex());
                    btnValider.setOnAction(e -> traiter(abs, StatutAbsence.VALIDEE));
                    btnRefuser.setOnAction(e -> traiter(abs, StatutAbsence.REFUSEE));
                    setGraphic(container);
                }
            }
        });
    }

    private void ouvrirFichiers(Absence abs) {
        for (String path : abs.getPiecesJointesPaths()) {
            try {
                Desktop.getDesktop().open(new File(path));
            } catch (Exception e) {
                System.err.println("Impossible d'ouvrir : " + path);
            }
        }
    }

    private void traiter(Absence abs, StatutAbsence nouveauStatut) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            Absence managed = em.merge(abs);
            managed.setStatut(nouveauStatut);
            managed.setDateValidation(LocalDateTime.now());
            // On récupère l'admin connecté via ta session
            managed.setValideePar((Administration) fr.univ.hyperplanning.app.SessionUtilisateur.get());

            em.getTransaction().commit();
            chargerDonnees(); // Rafraîchir le tableau
        } finally {
            em.close();
        }
    }

    private void chargerDonnees() {
        tableAbsences.setItems(FXCollections.observableArrayList(absenceDAO.findAbsencesAValider()));
    }
}