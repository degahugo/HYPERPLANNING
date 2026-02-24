package fr.univ.hyperplanning.model.gestion;

import fr.univ.hyperplanning.model.enums.StatutDemande;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.utilisateurs.Administration;
import fr.univ.hyperplanning.model.utilisateurs.Enseignant;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "demandes_modification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeModification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String description;

    @Column(nullable=false)
    private String motif;

    @Column(nullable=false)
    private Date dateDemande;

    @Column
    private Date dateTraitement;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private StatutDemande statut;

    @Column(nullable=false)
    private String motifRefus;

    @ManyToOne(optional = false)
    @JoinColumn(name = "demandeur_id")
    private Enseignant demandeur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cours_id")
    private Cours cours;

    @OneToMany(mappedBy="demandeModification", cascade=CascadeType.ALL, orphanRemoval=true)
    private Set<Notification> notifications = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "traitee_par_id")
    private Administration traiteePar;

}
