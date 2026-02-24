package fr.univ.hyperplanning.model.utilisateurs;

import fr.univ.hyperplanning.model.gestion.Absence;
import fr.univ.hyperplanning.model.gestion.DemandeModification;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "administrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Administration extends Utilisateur {

    @Column(nullable = false)
    private String service;

    @Column(nullable = false)
    private int niveauAcces;

    @ManyToOne
    @JoinColumn(name="traitee_par_id")
    private Administration traiteePar;

    @OneToMany(mappedBy="traiteePar")
    private Set<DemandeModification> demandesTraitees = new HashSet<>();

    @OneToMany(mappedBy = "valideePar")
    private Set<Absence> absencesTraitees = new HashSet<>();
}
