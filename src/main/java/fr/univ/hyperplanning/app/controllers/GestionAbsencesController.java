package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.app.SessionUtilisateur;
import fr.univ.hyperplanning.dao.AbsenceDAO;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.enums.StatutAbsence;
import fr.univ.hyperplanning.model.gestion.Absence;
import fr.univ.hyperplanning.model.utilisateurs.Administration;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import jakarta.persistence.EntityManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GestionAbsencesController {

    @FXML private TableView<Absence> tableAbsences;

    @FXML private TableColumn<Absence, String> colNom;
    @FXML private TableColumn<Absence, String> colPrenom;
    @FXML private TableColumn<Absence, String> colUtilisateurs;
    @FXML private TableColumn<Absence, String> colCours;
    @FXML private TableColumn<Absence, String> colDate;
    @FXML private TableColumn<Absence, String> colMotif;
    @FXML private TableColumn<Absence, String> colStatut;
    @FXML private TableColumn<Absence, String> colDateDeclaration;
    @FXML private TableColumn<Absence, String> colDateValidation;
    @FXML private TableColumn<Absence, String> colTraitePar;
    @FXML private TableColumn<Absence, String> colMotifRefus;

    @FXML private TableColumn<Absence, Void> colJustificatifs;
    @FXML private TableColumn<Absence, Void> colActions;

    @FXML private ComboBox<String> comboStatutFiltre;

    private final AbsenceDAO absenceDAO = new AbsenceDAO();

    @FXML
    public void initialize() {
        initialiserFiltre();
        configurerColonnes();
        chargerDonnees();
    }

    private void initialiserFiltre() {
        if (comboStatutFiltre == null) return;

        comboStatutFiltre.getItems().setAll(
                "Déclarées",
                "Validées",
                "Refusées",
                "Toutes"
        );
        comboStatutFiltre.setValue("Déclarées");
    }

    @FXML
    private void onFiltreChanged() {
        chargerDonnees();
    }

    private String formaterDateHeure(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private void configurerColonnes() {
        colNom.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getUtilisateurAbsent() != null
                                ? data.getValue().getUtilisateurAbsent().getNom()
                                : ""
                )
        );

        colPrenom.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getUtilisateurAbsent() != null
                                ? data.getValue().getUtilisateurAbsent().getPrenom()
                                : ""
                )
        );

        colUtilisateurs.setCellValueFactory(data -> {
            Utilisateur u = data.getValue().getUtilisateurAbsent();
            String role = switch (u.getRole()) {
                case ETUDIANT -> "Étudiant";
                case ENSEIGNANT, ENSEIGNANT_RESPONSABLE -> "Enseignant";
                case INTERVENANT -> "Intervenant";
                default -> "Utilisateur";
            };
            return new SimpleStringProperty(role);
        });

        colCours.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCours() != null
                                ? data.getValue().getCours().getIntitule()
                                : "N/A"
                )
        );

        colDate.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getDateAbsence() != null
                                ? data.getValue().getDateAbsence().toString()
                                : ""
                )
        );

        colMotif.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getMotif() != null
                                ? data.getValue().getMotif()
                                : ""
                )
        );

        colStatut.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getStatut() != null
                                ? data.getValue().getStatut().name()
                                : ""
                )
        );

        colDateDeclaration.setCellValueFactory(data ->
                new SimpleStringProperty(
                        formaterDateHeure(data.getValue().getDateDeclaration())
                )
        );

        colDateValidation.setCellValueFactory(data ->
                new SimpleStringProperty(
                        formaterDateHeure(data.getValue().getDateValidation())
                )
        );

        colTraitePar.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getValideePar() != null
                                ? data.getValue().getValideePar().getNom() + " " + data.getValue().getValideePar().getPrenom()
                                : ""
                )
        );

        colMotifRefus.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getMotifRefus() != null
                                ? data.getValue().getMotifRefus()
                                : ""
                )
        );

        configurerColonneJustificatifs();
        configurerColonneActions();
    }

    private void configurerColonneJustificatifs() {
        colJustificatifs.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button();

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Absence abs = getTableView().getItems().get(getIndex());
                int nb = abs.getPiecesJointesPaths() == null ? 0 : abs.getPiecesJointesPaths().size();
                btnVoir.setText("📁 Voir (" + nb + ")");
                btnVoir.setDisable(nb == 0);
                btnVoir.setOnAction(e -> ouvrirFichiers(abs));
                setGraphic(btnVoir);
            }
        });
    }

    private void configurerColonneActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnValider = new Button("✅");
            private final Button btnRefuser = new Button("❌");
            private final HBox container = new HBox(10, btnValider, btnRefuser);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Absence abs = getTableView().getItems().get(getIndex());

                if (abs.getStatut() != StatutAbsence.DECLAREE) {
                    setGraphic(null);
                    return;
                }

                btnValider.setOnAction(e -> traiter(abs, StatutAbsence.VALIDEE));
                btnRefuser.setOnAction(e -> ouvrirDialogueRefus(abs));
                setGraphic(container);
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
        if (abs.getPiecesJointesPaths() == null || abs.getPiecesJointesPaths().isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucun justificatif disponible.").show();
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(abs.getPiecesJointesPaths().get(0), abs.getPiecesJointesPaths());
        dialog.setTitle("Justificatifs");
        dialog.setHeaderText("Choisir un justificatif à ouvrir");
        dialog.setContentText("Fichier :");

        dialog.showAndWait().ifPresent(path -> {
            try {
                Desktop.getDesktop().open(new File(path));
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le fichier : " + path).show();
                e.printStackTrace();
            }
        });
    }

    private void traiter(Absence abs, StatutAbsence nouveauStatut) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();

            Absence managed = em.merge(abs);
            managed.setStatut(nouveauStatut);
            managed.setDateValidation(LocalDateTime.now());
            managed.setValideePar((Administration) SessionUtilisateur.get());

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
        String filtre = comboStatutFiltre != null ? comboStatutFiltre.getValue() : "Déclarées";

        List<Absence> absences;

        if ("Toutes".equals(filtre)) {
            absences = absenceDAO.findAllAbsences();
        } else if ("Validées".equals(filtre)) {
            absences = absenceDAO.findAbsencesByStatut(StatutAbsence.VALIDEE);
        } else if ("Refusées".equals(filtre)) {
            absences = absenceDAO.findAbsencesByStatut(StatutAbsence.REFUSEE);
        } else {
            absences = absenceDAO.findAbsencesByStatut(StatutAbsence.DECLAREE);
        }

        tableAbsences.setItems(FXCollections.observableArrayList(absences));
        mettreAJourVisibiliteColonnes(filtre);
    }

    private void mettreAJourVisibiliteColonnes(String filtre) {
        boolean estDeclaree = "Déclarées".equals(filtre);
        boolean estValidee = "Validées".equals(filtre);
        boolean estRefusee = "Refusées".equals(filtre);
        boolean estToutes = "Toutes".equals(filtre);

        colDate.setVisible(true);
        colDateDeclaration.setVisible(true);
        colStatut.setVisible(true);

        colDateValidation.setVisible(estValidee || estRefusee || estToutes);
        colTraitePar.setVisible(estValidee || estRefusee || estToutes);
        colMotifRefus.setVisible(estRefusee || estToutes);

        colActions.setVisible(estDeclaree);
    }
}