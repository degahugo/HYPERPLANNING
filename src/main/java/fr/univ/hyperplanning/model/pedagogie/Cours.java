package fr.univ.hyperplanning.model.pedagogie;


import fr.univ.hyperplanning.model.utilisateurs.Enseignant;
import fr.univ.hyperplanning.model.utilisateurs.Intervenant;
import fr.univ.hyperplanning.model.enums.TypeCours;
import fr.univ.hyperplanning.model.gestion.Absence;
import fr.univ.hyperplanning.model.gestion.Conflit;
import fr.univ.hyperplanning.model.gestion.Historique;
import fr.univ.hyperplanning.model.gestion.Notification;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="cours")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cours {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String intitule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCours typeCours;

    @Column(nullable=false)
    private java.time.LocalDateTime dateDebut;

    @Column(nullable=false)
    private java.time.LocalDateTime dateFin;

    @Column(nullable=false)
    private int duree;

    @Column(nullable=false)
    private boolean estCC;

    @Column(nullable=false)
    private boolean estAnnule;

    @Column(nullable=false)
    private String annotation;

    @Column(nullable = false)
    private boolean enConflit;

    @ManyToOne(optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private ModuleCours module;

    @ManyToOne(optional = false)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @ManyToMany
    @JoinTable(
            name = "cours_groupe",
            joinColumns = @JoinColumn(name = "cours_id"),
            inverseJoinColumns = @JoinColumn(name = "groupe_id")
    )
    private Set<Groupe> groupes = new HashSet<>();

    // rattrapage de (un cours peut être le rattrapage d’un autre)
    @ManyToOne
    @JoinColumn(name = "rattrapage_de_id")
    private Cours coursRattrapageDe;

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Absence> absences = new HashSet<>();

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Notification> notifications = new HashSet<>();

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Historique> historiques = new HashSet<>();

    // “peut avoir des conflits” (cours impactés par le conflit)
    @ManyToMany(mappedBy = "coursImpactes")
    private Set<Conflit> conflits = new HashSet<>();




    @ManyToMany(mappedBy = "coursEnseignes")
    private Set<Enseignant> enseignantsEnseignes = new HashSet<>();

    @ManyToMany(mappedBy = "coursInterventions")
    private Set<Intervenant> intervenantsInterventions = new HashSet<>();
}