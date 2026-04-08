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
import fr.univ.hyperplanning.app.SessionUtilisateur;

public class GestionAbsencesController {

    @FXML private TableView<Absence> tableAbsences;
    @FXML private TableColumn<Absence, String> colUtilisateurs, colNom, colPrenom, colCours, colDate, colMotif;
    @FXML private TableColumn<Absence, Void> colJustificatifs, colActions;

    private final AbsenceDAO absenceDAO = new AbsenceDAO();

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerDonnees();
    }

    private void configurerColonnes() {
        colNom.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getUtilisateurAbsent().getNom()
                )
        );

        colPrenom.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getUtilisateurAbsent().getPrenom()
                )
        );

        colUtilisateurs.setCellValueFactory(data -> {
            var u = data.getValue().getUtilisateurAbsent();

            String role = switch (u.getRole()) {
                case ETUDIANT -> "Étudiant";
                case ENSEIGNANT, ENSEIGNANT_RESPONSABLE -> "Enseignant";
                case INTERVENANT -> "Intervenant";
                default -> "Utilisateur";
            };

            return new SimpleStringProperty(role);
        });
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
                    btnRefuser.setOnAction(e -> ouvrirDialogueRefus(abs));                    setGraphic(container);
                }
            }
        });
    }

    private void ouvrirDialogueRefus(Absence abs) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Refus de l'absence");
        dialog.setHeaderText("Motif du refus");
        dialog.setContentText("Veuillez saisir le motif :");

        dialog.showAndWait().ifPresent(motif -> {
            String motifRefus = motif == null ? "" : motif.trim();
            if (!motifRefus.isEmpty()) {
                traiterRefus(abs, motifRefus);
            }
        });
    }

    private void traiterRefus(Absence abs, String motifRefus) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            Absence managed = em.merge(abs);
            managed.setStatut(StatutAbsence.REFUSEE);
            managed.setMotifRefus(motifRefus);
            managed.setDateValidation(LocalDateTime.now());
            managed.setValideePar((Administration) SessionUtilisateur.get());
            em.getTransaction().commit();
            chargerDonnees();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
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
            managed.setValideePar((Administration) SessionUtilisateur.get());

            // IMPORTANT
            if (nouveauStatut == StatutAbsence.VALIDEE) {
                managed.setMotifRefus(null);
            }

            em.getTransaction().commit();
            chargerDonnees();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    private void chargerDonnees() {
        tableAbsences.setItems(FXCollections.observableArrayList(absenceDAO.findAbsencesAValider()));
    }
}