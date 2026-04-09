package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.dao.GroupeDAO;
import fr.univ.hyperplanning.model.enums.NiveauGroupe;
import fr.univ.hyperplanning.model.enums.TypeGroupe;
import fr.univ.hyperplanning.model.pedagogie.Groupe;
import fr.univ.hyperplanning.model.pedagogie.SyllabusSemestre;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.util.List;

public class GestionGroupesController {

    @FXML private TextField txtCode;
    @FXML private TextField txtLibelle;
    @FXML private ComboBox<NiveauGroupe> comboNiveau;
    @FXML private ComboBox<TypeGroupe> comboTypeGrp;
    @FXML private TextField txtEffectifMax;
    @FXML private TextField txtAnnee;
    @FXML private ComboBox<SyllabusSemestre> comboSyllabus;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    @FXML private TableView<Groupe> tableGroupes;
    @FXML private TableColumn<Groupe, String>  colCode;
    @FXML private TableColumn<Groupe, String>  colLibelle;
    @FXML private TableColumn<Groupe, String>  colNiveau;
    @FXML private TableColumn<Groupe, String>  colTypeGrp;
    @FXML private TableColumn<Groupe, Integer> colEffMax;
    @FXML private TableColumn<Groupe, Integer> colEffActuel;
    @FXML private TableColumn<Groupe, Integer> colAnnee;
    @FXML private TableColumn<Groupe, String>  colSyllabus;
    @FXML private TableColumn<Groupe, Void>    colActions;

    private final GroupeDAO groupeDAO = new GroupeDAO();
    private final ObservableList<Groupe> groupes = FXCollections.observableArrayList();
    private Groupe groupeEnEdition = null;

    @FXML
    public void initialize() {
        // Colonnes
        colCode.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getCode()));
        colLibelle.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getLibelle()));
        colNiveau.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getNiveau().name()));
        colTypeGrp.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getTypeGroupe().name()));
        colEffMax.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getEffectifMax()).asObject());
        colEffActuel.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getEffectifActuel()).asObject());
        colAnnee.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getAnneeUniversitaire()).asObject());
        colSyllabus.setCellValueFactory(cd -> {
            SyllabusSemestre s = cd.getValue().getSyllabus();
            String label = (s != null)
                    ? s.getNiveau().name() + " S" + s.getSemestre() + " (" + s.getAnneeUniversitaire() + ")"
                    : "—";
            return new SimpleStringProperty(label);
        });

        ajouterColonneActions();

        // ComboBox enums
        comboNiveau.setItems(FXCollections.observableArrayList(NiveauGroupe.values()));
        comboTypeGrp.setItems(FXCollections.observableArrayList(TypeGroupe.values()));

        // ComboBox syllabus avec affichage lisible
        List<SyllabusSemestre> syllabusList = groupeDAO.findAllSyllabus();
        comboSyllabus.setItems(FXCollections.observableArrayList(syllabusList));
        comboSyllabus.setConverter(new StringConverter<>() {
            @Override
            public String toString(SyllabusSemestre s) {
                return s == null ? "" : s.getNiveau().name() + " S" + s.getSemestre()
                        + " (" + s.getAnneeUniversitaire() + ")";
            }

            @Override
            public SyllabusSemestre fromString(String string) {
                return null;
            }
        });

        chargerGroupes();
        tableGroupes.setItems(groupes);
    }

    // ── ACTIONS FORMULAIRE ─────────────────────────────────

    @FXML
    private void onSave() {
        String code     = txtCode.getText().trim();
        String libelle  = txtLibelle.getText().trim();
        NiveauGroupe niv = comboNiveau.getValue();
        TypeGroupe type  = comboTypeGrp.getValue();
        String effStr   = txtEffectifMax.getText().trim();
        String anneeStr = txtAnnee.getText().trim();
        SyllabusSemestre syllabus = comboSyllabus.getValue();

        if (code.isEmpty() || libelle.isEmpty() || niv == null || type == null
                || effStr.isEmpty() || anneeStr.isEmpty() || syllabus == null) {
            alerte("Champs requis", "Veuillez remplir tous les champs.");
            return;
        }

        int effectifMax, annee;
        try {
            effectifMax = Integer.parseInt(effStr);
            annee = Integer.parseInt(anneeStr);
        } catch (NumberFormatException e) {
            alerte("Erreur", "L'effectif max et l'année doivent être des nombres entiers.");
            return;
        }

        try {
            if (groupeEnEdition == null) {
                Groupe nouveau = Groupe.builder()
                        .code(code)
                        .libelle(libelle)
                        .niveau(niv)
                        .typeGroupe(type)
                        .effectifMax(effectifMax)
                        .effectifActuel(0)
                        .anneeUniversitaire(annee)
                        .syllabus(syllabus)
                        .build();
                groupeDAO.save(nouveau);
            } else {
                groupeEnEdition.setCode(code);
                groupeEnEdition.setLibelle(libelle);
                groupeEnEdition.setNiveau(niv);
                groupeEnEdition.setTypeGroupe(type);
                groupeEnEdition.setEffectifMax(effectifMax);
                groupeEnEdition.setAnneeUniversitaire(annee);
                groupeEnEdition.setSyllabus(syllabus);
                groupeDAO.update(groupeEnEdition);
            }
            resetFormulaire();
            chargerGroupes();
        } catch (Exception e) {
            e.printStackTrace();
            alerte("Erreur", "Impossible d'enregistrer : " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        resetFormulaire();
    }

    // ── COLONNE ACTIONS ────────────────────────────────────

    private void ajouterColonneActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️ Modifier");
            private final Button btnDel  = new Button("🗑️ Supprimer");
            private final HBox box = new HBox(8, btnEdit, btnDel);

            {
                box.setAlignment(Pos.CENTER);
                btnEdit.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 11;");
                btnDel.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 11;");

                btnEdit.setOnAction(e -> {
                    Groupe g = getTableView().getItems().get(getIndex());
                    remplirFormulaire(g);
                });
                btnDel.setOnAction(e -> {
                    Groupe g = getTableView().getItems().get(getIndex());
                    confirmerSuppression(g);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ── HELPERS ────────────────────────────────────────────

    private void chargerGroupes() {
        groupes.setAll(groupeDAO.findAll());
    }

    private void remplirFormulaire(Groupe g) {
        groupeEnEdition = g;
        txtCode.setText(g.getCode());
        txtLibelle.setText(g.getLibelle());
        comboNiveau.setValue(g.getNiveau());
        comboTypeGrp.setValue(g.getTypeGroupe());
        txtEffectifMax.setText(String.valueOf(g.getEffectifMax()));
        txtAnnee.setText(String.valueOf(g.getAnneeUniversitaire()));
        comboSyllabus.setValue(g.getSyllabus());
        btnSave.setText("💾 Enregistrer");
        btnCancel.setVisible(true);
        btnCancel.setManaged(true);
    }

    private void resetFormulaire() {
        groupeEnEdition = null;
        txtCode.clear();
        txtLibelle.clear();
        comboNiveau.setValue(null);
        comboTypeGrp.setValue(null);
        txtEffectifMax.clear();
        txtAnnee.clear();
        comboSyllabus.setValue(null);
        btnSave.setText("➕ Ajouter");
        btnCancel.setVisible(false);
        btnCancel.setManaged(false);
    }

    private void confirmerSuppression(Groupe g) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le groupe « " + g.getLibelle() + " » ?\n"
                        + "⚠️ Les étudiants et cours liés ne seront pas supprimés.",
                ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    groupeDAO.delete(g.getId());
                    chargerGroupes();
                } catch (Exception e) {
                    alerte("Erreur", "Impossible de supprimer (des cours sont peut-être encore liés).");
                }
            }
        });
    }

    private void alerte(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titre);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
