package fr.univ.hyperplanning.model;

public class ConflictInfo {
    private long id;
    private String description;

    public ConflictInfo(long id, String description) {
        this.id = id;
        this.description = description;
    }

    // Très important pour la TableView : les Getters
    public long getId() { return id; }
    public String getDescription() { return description; }
}