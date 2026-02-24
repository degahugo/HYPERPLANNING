package fr.univ.hyperplanning.model.utilisateurs;

import fr.univ.hyperplanning.model.gestion.DemandeModification;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.pedagogie.Groupe;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "enseignants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Enseignant extends Utilisateur {

    @Column(nullable = false)
    private String numeroEmploye;

    @Column(nullable = false)
    private String specialite;

    @Column(nullable = false)
    private boolean estResponsable;

    @ManyToMany
    @JoinTable(
            name = "enseignant_cours",
            joinColumns = @JoinColumn(name = "enseignant_id"),
            inverseJoinColumns = @JoinColumn(name = "cours_id")
    )
    private Set<Cours> coursEnseignes = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "enseignant_groupe",
            joinColumns = @JoinColumn(name = "enseignant_id"),
            inverseJoinColumns = @JoinColumn(name = "groupe_id")
    )
    private Set<Groupe> groupesEncadres = new HashSet<>();

    @OneToMany(mappedBy = "demandeur")
    private Set<DemandeModification> demandes = new HashSet<>();
}
