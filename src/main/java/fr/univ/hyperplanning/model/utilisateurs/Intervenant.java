package fr.univ.hyperplanning.model.utilisateurs;

import fr.univ.hyperplanning.model.pedagogie.Cours;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "intervenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Intervenant extends Utilisateur {

    @Column(nullable = false)
    private String entreprise;

    @Column(nullable = false)
    private String contrat;

    @ManyToMany
    @JoinTable(
            name = "intervenant_cours",
            joinColumns = @JoinColumn(name = "intervenant_id"),
            inverseJoinColumns = @JoinColumn(name = "cours_id")
    )
    private Set<Cours> coursInterventions = new HashSet<>();
}
