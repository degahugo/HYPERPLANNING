package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.dao.SalleDAO;
import fr.univ.hyperplanning.model.enums.TypeSalle;
import fr.univ.hyperplanning.model.pedagogie.Salle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class GestionSallesController {

    @FXML private TextField txtNumero;
    @FXML private TextField txtBatiment;
    @FXML private TextField txtCapacite;
    @FXML private ComboBox<TypeSalle> comboTypeSalle;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    @FXML private TableView<Salle> tableSalles;
    @FXML private TableColumn<Salle, String>  colNumero;
    @FXML private TableColumn<Salle, String>  colBatiment;
    @FXML private TableColumn<Salle, Integer> colCapacite;
    @FXML private TableColumn<Salle, String>  colType;
    @FXML private TableColumn<Salle, Void>    colActions;

    private final SalleDAO salleDAO = new SalleDAO();
    private final ObservableList<Salle> salles = FXCollections.observableArrayList();
    private Salle salleEnEdition = null; // null = mode création

    @FXML
    public void initialize() {
        // Colonnes
        colNumero.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNumero()));
        colBatiment.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getBatiment()));
        colCapacite.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getCapacite()).asObject());
        colType.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTypeSalle().name()));

        ajouterColonneActions();

        // ComboBox types
        comboTypeSalle.setItems(FXCollections.observableArrayList(TypeSalle.values()));

        // Charger les données
        chargerSalles();
        tableSalles.setItems(salles);
    }

    // Action Formulaire (Créer une salle)

    @FXML
    private void onSave() {
        String numero   = txtNumero.getText().trim();
        String batiment = txtBatiment.getText().trim();
        String capStr   = txtCapacite.getText().trim();
        TypeSalle type  = comboTypeSalle.getValue();

        if (numero.isEmpty() || batiment.isEmpty() || capStr.isEmpty() || type == null) {
            alerte("Champs requis", "Veuillez remplir tous les champs.");
            return;
        }

        int capacite;
        try {
            capacite = Integer.parseInt(capStr);
        } catch (NumberFormatException e) {
            alerte("Erreur", "La capacité doit être un nombre entier.");
            return;
        }

        try {
            if (salleEnEdition == null) {
                // Créer la salle
                Salle nouvelle = Salle.builder()
                        .numero(numero)
                        .batiment(batiment)
                        .capacite(capacite)
                        .typeSalle(type)
                        .actif(true)
                        .build();
                salleDAO.save(nouvelle);
            } else {
                // Modifier une salle
                salleEnEdition.setNumero(numero);
                salleEnEdition.setBatiment(batiment);
                salleEnEdition.setCapacite(capacite);
                salleEnEdition.setTypeSalle(type);
                salleDAO.update(salleEnEdition);
            }
            resetFormulaire();
            chargerSalles();
        } catch (Exception e) {
            e.printStackTrace();
            alerte("Erreur", "Impossible d'enregistrer : " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        resetFormulaire();
    }

    // Colonne pour modifier / supprimer une salle

    private void ajouterColonneActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDel  = new Button("Supprimer");
            private final HBox box = new HBox(8, btnEdit, btnDel);

            {
                box.setAlignment(Pos.CENTER);
                btnEdit.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 11;");
                btnDel.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 11;");

                btnEdit.setOnAction(e -> {
                    Salle s = getTableView().getItems().get(getIndex());
                    remplirFormulaire(s);
                });
                btnDel.setOnAction(e -> {
                    Salle s = getTableView().getItems().get(getIndex());
                    confirmerSuppression(s);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }


    private void chargerSalles() {
        salles.setAll(salleDAO.findAll());
    }

    private void remplirFormulaire(Salle s) {
        salleEnEdition = s;
        txtNumero.setText(s.getNumero());
        txtBatiment.setText(s.getBatiment());
        txtCapacite.setText(String.valueOf(s.getCapacite()));
        comboTypeSalle.setValue(s.getTypeSalle());
        btnSave.setText("Enregistrer");
        btnCancel.setVisible(true);
        btnCancel.setManaged(true);
    }

    private void resetFormulaire() {
        salleEnEdition = null;
        txtNumero.clear();
        txtBatiment.clear();
        txtCapacite.clear();
        comboTypeSalle.setValue(null);
        btnSave.setText("Ajouter");
        btnCancel.setVisible(false);
        btnCancel.setManaged(false);
    }

    private void confirmerSuppression(Salle s) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la salle « " + s.getNumero() + " » ?",
                ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                salleDAO.delete(s.getId());
                chargerSalles();
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
