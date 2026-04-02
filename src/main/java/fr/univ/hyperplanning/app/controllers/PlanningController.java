package fr.univ.hyperplanning.app.controllers;

import fr.univ.hyperplanning.app.SessionUtilisateur;
import fr.univ.hyperplanning.dao.CoursDAO;
import fr.univ.hyperplanning.dao.SalleDAO;
import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.enums.NiveauGroupe;
import fr.univ.hyperplanning.model.enums.RoleUtilisateur;
import fr.univ.hyperplanning.model.enums.TypeCours;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.pedagogie.Salle;
import fr.univ.hyperplanning.model.utilisateurs.Enseignant;
import fr.univ.hyperplanning.model.utilisateurs.Intervenant;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PlanningController {

    private final CoursDAO coursDAO = new CoursDAO();
    private final SalleDAO salleDAO = new SalleDAO();

    @FXML private Label weekLabel;
    @FXML private HBox planningContainer;
    @FXML private Button btnWeek, btnDay;

    // Barre de filtres complète
    @FXML private HBox filterBar;
    @FXML private ComboBox<String> comboPromo;
    @FXML private ComboBox<String> comboGroupe;
    @FXML private ComboBox<String> comboSalle;
    @FXML private ComboBox<TypeCours> comboType;

    private LocalDate currentDate;
    private boolean isWeekView = true;

    // États mémoires pour les filtres
    private NiveauGroupe selectedPromotion = null;
    private String selectedGroupe = null;
    private String selectedSalleNumero = null;

    @FXML
    public void initialize() {
        currentDate = LocalDate.now();

        // Rendre les filtres visibles pour tous les utilisateurs
        if (filterBar != null) {
            filterBar.setManaged(true);
            filterBar.setVisible(true);
        }

        initialiserFiltres();
        updateToggleButtonStyles();
        chargerDonnees();
    }

    private void initialiserFiltres() {
        // 1. Initialisation des Promotions (NiveauGroupe)
        comboPromo.getItems().clear();
        comboPromo.getItems().add("Toutes les promotions");
        for (NiveauGroupe n : NiveauGroupe.values()) {
            comboPromo.getItems().add(n.name());
        }
        comboPromo.setValue("Toutes les promotions");

        // 2. Initialisation des Groupes (dépendra de la promo choisie)
        rafraichirComboGroupes();

        // 3. Initialisation des Salles (Numéros)
        comboSalle.getItems().clear();
        comboSalle.getItems().add("Toutes les salles");
        comboSalle.getItems().addAll(salleDAO.findAllNumeros());
        comboSalle.setValue("Toutes les salles");

        // 4. Initialisation des Types de cours (CM, TD, TP)
        if (comboType != null) {
            comboType.setItems(FXCollections.observableArrayList(TypeCours.values()));
        }
    }

    private void rafraichirComboGroupes() {
        comboGroupe.getItems().clear();
        comboGroupe.getItems().add("Tous les groupes");
        if (selectedPromotion == null) {
            comboGroupe.getItems().addAll(coursDAO.findAllGroupCodes());
        } else {
            comboGroupe.getItems().addAll(coursDAO.findGroupCodesByPromotion(selectedPromotion));
        }
        comboGroupe.setValue("Tous les groupes");
    }

    // --- GESTION DES ACTIONS DES FILTRES ---

    @FXML
    private void onPromotionChanged() {
        String val = comboPromo.getValue();
        selectedPromotion = (val == null || val.equals("Toutes les promotions")) ? null : NiveauGroupe.valueOf(val);
        rafraichirComboGroupes();
        chargerDonnees();
    }

    @FXML
    private void onGroupeChanged() {
        String val = comboGroupe.getValue();
        selectedGroupe = (val == null || val.equals("Tous les groupes")) ? null : val;
        chargerDonnees();
    }

    @FXML
    private void onSalleChanged() {
        String val = comboSalle.getValue();
        selectedSalleNumero = (val == null || val.equals("Toutes les salles")) ? null : val;
        chargerDonnees();
    }

    @FXML
    private void onTypeChanged() {
        chargerDonnees();
    }

    @FXML
    private void onResetFilters() {
        selectedPromotion = null;
        selectedGroupe = null;
        selectedSalleNumero = null;

        comboPromo.setValue("Toutes les promotions");
        comboSalle.setValue("Toutes les salles");
        if (comboType != null) comboType.setValue(null);

        rafraichirComboGroupes();
        chargerDonnees();
    }

    // LOGIQUE DE RÉCUPÉRATION ET CALCUL

    private void chargerDonnees() {
        LocalDate debut = currentDate.with(DayOfWeek.MONDAY);
        LocalDate fin = debut.plusDays(6);

        // Titre dynamique de la période
        String dateTexte = debut.getDayOfMonth() + " " + debut.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH) +
                " - " + fin.getDayOfMonth() + " " + fin.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " + fin.getYear();
        weekLabel.setText(dateTexte);

        if (isWeekView) chargerSemaine(currentDate);
        else chargerVueJourSemaine(currentDate);
    }

    private List<Cours> obtenirCoursFiltres(LocalDate debut, LocalDate fin) {
        Utilisateur user = SessionUtilisateur.get();
        List<Cours> base;

        // Si un filtre est actif, on cherche globalement
        if (selectedPromotion != null || selectedGroupe != null || selectedSalleNumero != null || (comboType != null && comboType.getValue() != null)) {
            base = coursDAO.findByDateRange(debut, fin.plusDays(1));
        }
        // Sinon, on affiche par défaut le planning personnel de l'utilisateur
        else if (user != null) {
            base = switch (user.getRole()) {
                case ETUDIANT -> coursDAO.findByEtudiant(user.getId(), debut, fin.plusDays(1));
                case ENSEIGNANT, ENSEIGNANT_RESPONSABLE -> coursDAO.findByEnseignant(user.getId(), debut, fin.plusDays(1));
                case INTERVENANT -> coursDAO.findByIntervenant(user.getId(), debut, fin.plusDays(1));
                default -> coursDAO.findByDateRange(debut, fin.plusDays(1));
            };
        } else {
            base = List.of();
        }

        // Filtrage fin par Stream
        return base.stream()
                .filter(c -> selectedSalleNumero == null || (c.getSalle() != null && c.getSalle().getNumero().equals(selectedSalleNumero)))
                .filter(c -> selectedPromotion == null || c.getGroupes().stream().anyMatch(g -> g.getNiveau() == selectedPromotion))
                .filter(c -> selectedGroupe == null || c.getGroupes().stream().anyMatch(g -> g.getCode().equals(selectedGroupe)))
                .filter(c -> comboType == null || comboType.getValue() == null || c.getTypeCours() == comboType.getValue())
                .collect(Collectors.toList());
    }

    private String obtenirNomProf(Cours c) {
        StringBuilder sb = new StringBuilder();
        if (c.getEnseignantsEnseignes() != null) {
            for (Enseignant e : c.getEnseignantsEnseignes()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("👤 ").append(e.getNom().toUpperCase()).append(" ").append(e.getPrenom().substring(0, 1)).append(".");
            }
        }
        if (c.getIntervenantsInterventions() != null) {
            for (Intervenant i : c.getIntervenantsInterventions()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("👤 ").append(i.getNom().toUpperCase()).append(" (Ext.)");
            }
        }
        return sb.length() > 0 ? sb.toString() : "👤 Non assigné";
    }

    // --- VUES (SEMAINE ET JOUR) ---

    private void chargerSemaine(LocalDate date) {
        LocalDate debut = date.with(DayOfWeek.MONDAY);
        List<Cours> cours = obtenirCoursFiltres(debut, debut.plusDays(6));
        planningContainer.getChildren().clear();

        VBox mainGrid = new VBox(0);
        double colWidth = 140;

        // Entête des jours
        HBox header = new HBox(0);
        header.getChildren().add(creerSpacerHeure());
        for (int i = 0; i < 7; i++) {
            header.getChildren().add(creerEnteteJour(debut.plusDays(i), colWidth));
        }
        mainGrid.getChildren().add(header);

        // Corps du planning
        HBox body = new HBox(0);
        body.getChildren().add(creerColonneHeures());

        for (int i = 0; i < 7; i++) {
            LocalDate jour = debut.plusDays(i);
            Pane dayPane = new Pane();
            dayPane.setMinWidth(colWidth);
            if (jour.equals(LocalDate.now())) dayPane.setStyle("-fx-background-color: #F8FAFC;");

            // Lignes horizontales des heures
            for (int h = 0; h <= 10; h++) {
                Line line = new Line(0, h * 80, colWidth, h * 80);
                line.setStroke(Color.web("#F1F5F9"));
                dayPane.getChildren().add(line);
            }

            // Blocs de cours
            for (Cours c : cours) {
                if (c.getDateDebut().toLocalDate().equals(jour)) {
                    double startY = ((c.getDateDebut().getHour() - 8) * 80) + (c.getDateDebut().getMinute() * 80.0 / 60.0);
                    double hCell = ((c.getDateFin().getHour() - c.getDateDebut().getHour()) * 80) + ((c.getDateFin().getMinute() - c.getDateDebut().getMinute()) * 80.0 / 60.0);
                    dayPane.getChildren().add(creerBlocSemaine(c, colWidth - 8, hCell, startY));
                }
            }
            body.getChildren().add(dayPane);
        }
        mainGrid.getChildren().add(body);
        planningContainer.getChildren().add(mainGrid);
    }

    private void chargerVueJourSemaine(LocalDate date) {
        planningContainer.getChildren().clear();
        LocalDate debut = date.with(DayOfWeek.MONDAY);
        List<Cours> tousLesCours = obtenirCoursFiltres(debut, debut.plusDays(6));

        FlowPane gridDays = new FlowPane(20, 20);
        gridDays.setPadding(new Insets(10));
        gridDays.setPrefWrapLength(1200);

        for (int i = 0; i < 7; i++) {
            LocalDate jour = debut.plusDays(i);
            VBox dayColumn = new VBox(15);
            dayColumn.setPrefWidth(380);
            boolean isToday = jour.equals(LocalDate.now());

            dayColumn.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 25; -fx-background-radius: 20; -fx-border-color: " + (isToday ? "#3B82F6" : "#E5E7EB") + "; -fx-border-width: 1;");

            Label title = new Label(capitalize(jour.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH)) + " " + jour.getDayOfMonth() + "/" + jour.getMonthValue());
            title.setStyle("-fx-font-size: 20; -fx-font-weight: 900; -fx-text-fill: " + (isToday ? "#3B82F6" : "#111827") + ";");
            dayColumn.getChildren().add(title);

            List<Cours> coursDuJour = tousLesCours.stream().filter(c -> c.getDateDebut().toLocalDate().equals(jour)).collect(Collectors.toList());
            if (coursDuJour.isEmpty()) {
                Label vide = new Label("Aucun cours");
                vide.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic;");
                dayColumn.getChildren().add(vide);
            } else {
                for (Cours c : coursDuJour) dayColumn.getChildren().add(creerCarteDetaillee(c));
            }
            gridDays.getChildren().add(dayColumn);
        }
        planningContainer.getChildren().add(gridDays);
    }

    // --- COMPOSANTS GRAPHIQUES ---

    private VBox creerBlocSemaine(Cours c, double width, double height, double y) {
        VBox bloc = new VBox(2);
        bloc.setLayoutX(4); bloc.setLayoutY(y);
        bloc.setPrefSize(width, height - 2);
        bloc.setStyle("-fx-background-color: " + getHexColor(c) + "; -fx-background-radius: 8; -fx-padding: 8; -fx-cursor: hand;");
        bloc.setOnMouseClicked(e -> afficherDetailsCours(c));

        Label code = new Label(c.getIntitule().toUpperCase());
        code.setStyle("-fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 10;");

        Label salle = new Label("📍 " + c.getSalle().getNumero());
        salle.setStyle("-fx-text-fill: white; -fx-font-size: 9; -fx-font-weight: bold;");

        Label prof = new Label(obtenirNomProf(c));
        prof.setStyle("-fx-text-fill: white; -fx-font-size: 9; -fx-opacity: 0.9;");
        prof.setWrapText(true);

        bloc.getChildren().addAll(code, salle, prof);
        return bloc;
    }

    private VBox creerCarteDetaillee(Cours c) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-padding: 20;");

        HBox root = new HBox(15);
        Region sideBar = new Region();
        sideBar.setMinWidth(4);
        sideBar.setStyle("-fx-background-color: " + getHexColor(c) + "; -fx-background-radius: 2;");

        VBox content = new VBox(10);
        content.setPrefWidth(320);
        Label title = new Label(c.getIntitule());
        title.setStyle("-fx-font-size: 17; -fx-font-weight: bold; -fx-text-fill: #111827;");
        title.setWrapText(true);

        Label badge = new Label(getTypeLabel(c));
        badge.setStyle("-fx-background-color: " + getBadgeBgColor(c) + "; -fx-text-fill: " + getHexColor(c) + "; -fx-padding: 4 12; -fx-background-radius: 10; -fx-font-size: 11; -fx-font-weight: bold;");

        VBox infoBox = new VBox(8);
        Label profs = new Label(obtenirNomProf(c));
        profs.setWrapText(true);
        profs.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13;");

        infoBox.getChildren().addAll(
                creerInfoLigne("🕒", c.getDateDebut().toLocalTime() + " - " + c.getDateFin().toLocalTime()),
                creerInfoLigne("📍", c.getSalle().getNumero()),
                profs
        );

        content.getChildren().addAll(title, badge, infoBox);
        root.getChildren().addAll(sideBar, content);
        card.getChildren().add(root);
        return card;
    }

    private HBox creerInfoLigne(String icon, String text) {
        HBox h = new HBox(10); h.setAlignment(Pos.CENTER_LEFT);
        Label t = new Label(text); t.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13; -fx-font-weight: 500;");
        h.getChildren().addAll(new Label(icon), t);
        return h;
    }



    private String getHexColor(Cours c) {
        String type = c.getTypeCours().name();
        if (type.equals("CM")) return "#3B82F6"; // Bleu
        if (type.equals("TD")) return "#10B981"; // Vert
        if (type.equals("TP")) return "#A855F7"; // Violet
        return "#6B7280";
    }

    private String getBadgeBgColor(Cours c) {
        String type = c.getTypeCours().name();
        if (type.equals("CM")) return "#EFF6FF";
        if (type.equals("TD")) return "#ECFDF5";
        if (type.equals("TP")) return "#F5F3FF";
        return "#F3F4F6";
    }

    private Label creerSpacerHeure() {
        Label s = new Label("Heure"); s.setMinWidth(70); s.setStyle("-fx-padding: 10; -fx-text-fill: #9CA3AF; -fx-font-size: 13;");
        return s;
    }

    private VBox creerEnteteJour(LocalDate jour, double width) {
        VBox v = new VBox(2); v.setAlignment(Pos.CENTER); v.setMinWidth(width); v.setPadding(new Insets(10));
        boolean isToday = jour.equals(LocalDate.now());
        Label n = new Label(capitalize(jour.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH)));
        Label d = new Label(jour.getDayOfMonth() + "/" + jour.getMonthValue());
        n.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (isToday ? "#3B82F6" : "#4B5563") + ";");
        if (isToday) v.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 12 12 0 0;");
        v.getChildren().addAll(n, d);
        return v;
    }

    private VBox creerColonneHeures() {
        VBox v = new VBox(0);
        for (int h = 8; h <= 18; h++) {
            Label l = new Label(String.format("%02d:00", h));
            l.setMinHeight(80); l.setMinWidth(70); l.setAlignment(Pos.TOP_CENTER);
            l.setStyle("-fx-text-fill: #9CA3AF; -fx-padding: 5 0 0 0;");
            v.getChildren().add(l);
        }
        return v;
    }

    private String capitalize(String s) { return s.isEmpty() ? "" : s.substring(0,1).toUpperCase() + s.substring(1); }
    private String getTypeLabel(Cours c) { return c.getTypeCours().name().equals("CM") ? "Cours Magistral" : "Travaux Dirigés / Pratiques"; }

    private void updateToggleButtonStyles() {
        String active = "-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 0); -fx-font-weight: bold; -fx-text-fill: #111827;";
        String inactive = "-fx-background-color: transparent; -fx-text-fill: #9CA3AF;";
        btnWeek.setStyle(isWeekView ? active : inactive);
        btnDay.setStyle(!isWeekView ? active : inactive);
    }

    private void afficherDetailsCours(Cours c) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du cours");
        alert.setHeaderText(c.getIntitule());
        alert.setContentText("Salle : " + c.getSalle().getNumero() + "\nHoraire : " + c.getDateDebut().toLocalTime() + " - " + c.getDateFin().toLocalTime() + "\n\nResponsables :\n" + obtenirNomProf(c));
        alert.show();
    }

    @FXML private void onPrevWeek() { currentDate = currentDate.minusWeeks(1); chargerDonnees(); }
    @FXML private void onNextWeek() { currentDate = currentDate.plusWeeks(1); chargerDonnees(); }
    @FXML private void onWeekView() { isWeekView = true; updateToggleButtonStyles(); chargerDonnees(); }
    @FXML private void onDayView() { isWeekView = false; updateToggleButtonStyles(); chargerDonnees(); }
}