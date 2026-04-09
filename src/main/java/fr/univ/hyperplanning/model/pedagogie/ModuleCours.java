package fr.univ.hyperplanning.model.pedagogie;
import fr.univ.hyperplanning.model.utilisateurs.Enseignant;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="modules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ModuleCours {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String code;

    @Column(nullable=false)
    private String nom;

    @Column(nullable=false)
    private int nombreHeures;

    @Column(nullable=false)
    private float coefficient;

    @Column(nullable=false)
    private int semestre;

    @ManyToOne(optional = false)
    @JoinColumn(name = "syllabus_id", nullable = false)
    private SyllabusSemestre syllabus;

    @OneToMany(mappedBy = "module")
    private Set<Cours> cours = new HashSet<>();

    // Dans ModuleCours.java
    @ManyToOne
    @JoinColumn(name = "enseignant_id") // Clé étrangère vers la table des enseignants
    private Enseignant responsable;

    @Override
    public String toString() {
        return this.nom; // Utilise l'attribut 'nom' de ton entité
    }
}