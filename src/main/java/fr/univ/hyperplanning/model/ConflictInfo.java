package fr.univ.hyperplanning.model;
public class ConflictInfo {
    private long id;
    private String type;        // SALLE, GROUPE, ou ENSEIGNANT
    private String coursConcerne;
    private String salle;
    private String groupe;
    private String enseignant;
    private String description;

    public ConflictInfo(long id, String type, String cours1, String cours2, String salle, String groupe, String ens) {
        this.id = id;
        this.type = type;
        this.coursConcerne = cours1 + " vs " + cours2;
        this.salle = (salle != null) ? salle : "N/A";
        this.groupe = (groupe != null) ? groupe : "N/A";
        this.enseignant = (ens != null) ? ens : "Non assigné";
        this.description = String.format("Conflit %s sur le créneau.", type);
    }

    // Getters pour la TableView (Tâche 2 et 3)
    public long getId() { return id; }
    public String getType() { return type; }
    public String getCoursConcerne() { return coursConcerne; }
    public String getSalle() { return salle; }
    public String getGroupe() { return groupe; }
    public String getEnseignant() { return enseignant; }
    public String getDescription() { return description; }
}