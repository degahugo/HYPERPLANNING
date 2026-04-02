package fr.univ.hyperplanning.model.gestion;

import fr.univ.hyperplanning.model.enums.StatutAbsence;
import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.utilisateurs.Administration;
import fr.univ.hyperplanning.model.utilisateurs.Etudiant;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "absences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Absence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Date dateAbsence;

    @Column(nullable=false)
    private String motif;

    // Pour gérer plusieurs fichiers justificatifs
    @ElementCollection
    @CollectionTable(name = "absence_pieces_jointes", joinColumns = @JoinColumn(name = "absence_id"))
    @Column(name = "fichier_path")
    private java.util.List<String> piecesJointesPaths = new java.util.ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private StatutAbsence statut;

    @Column(nullable=false)
    private LocalDateTime dateDeclaration;

    @Column
    private LocalDateTime dateValidation;

    @Column
    private String motifRefus;


    @ManyToOne(optional = false)
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    @ManyToOne(optional = false)
    @JoinColumn(name = "etudiant_absent_id", nullable = false)
    private Etudiant etudiantAbsent;

    @ManyToOne(optional = false)
    @JoinColumn(name = "declaree_par_id", nullable = false)
    private Utilisateur declareePar;

    @ManyToOne
    @JoinColumn(name = "validee_par_admin_id")
    private Administration valideePar;

    @OneToMany(mappedBy = "absence", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Notification> notifications = new HashSet<>();

}
