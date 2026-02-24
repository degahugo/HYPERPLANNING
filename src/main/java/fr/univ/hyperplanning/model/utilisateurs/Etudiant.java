package fr.univ.hyperplanning.model.utilisateurs;

import fr.univ.hyperplanning.model.gestion.Absence;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.pedagogie.Groupe;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "etudiants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Etudiant extends Utilisateur {

    @Column(nullable = false, unique = true)
    private String numeroEtudiant;

    @Column(nullable = false)
    private int anneeEntree;

    @OneToMany(mappedBy = "etudiantAbsent")
    private java.util.Set<Absence> absences= new java.util.HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "etudiant_groupe",
            joinColumns = @JoinColumn(name = "etudiant_id"),
            inverseJoinColumns = @JoinColumn(name = "groupe_id")
    )
    private Set<Groupe> groupes = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "etudiant_cours",
            joinColumns = @JoinColumn(name = "etudiant_id"),
            inverseJoinColumns = @JoinColumn(name = "cours_id")
    )
    private Set<Cours> cours = new HashSet<>();
}
