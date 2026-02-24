package fr.univ.hyperplanning.model.gestion;

import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.pedagogie.Groupe;
import fr.univ.hyperplanning.model.pedagogie.Salle;
import fr.univ.hyperplanning.model.utilisateurs.Enseignant;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "conflits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conflit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String typeConflit;

    @Column(nullable=false)
    private String description;

    @Column(nullable=false)
    private Date dateDetection;

    @Column(nullable=false)
    private boolean estResolu;

    @Column(nullable=false)
    private Date dateResolution;

    @ManyToOne
    @JoinColumn(name = "source_cours_id")
    private Cours sourceCours;

    @ManyToOne
    @JoinColumn(name = "source_salle_id")
    private Salle sourceSalle;

    @ManyToOne
    @JoinColumn(name = "source_enseignant_id")
    private Enseignant sourceEnseignant;

    @ManyToOne
    @JoinColumn(name = "source_groupe_id")
    private Groupe sourceGroupe;

    // Cours impactés par ce conflit
    @ManyToMany
    @JoinTable(
            name = "conflit_cours",
            joinColumns = @JoinColumn(name = "conflit_id"),
            inverseJoinColumns = @JoinColumn(name = "cours_id")
    )
    private Set<Cours> coursImpactes = new HashSet<>();

}
