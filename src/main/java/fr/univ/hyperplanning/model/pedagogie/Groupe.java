package fr.univ.hyperplanning.model.pedagogie;

import fr.univ.hyperplanning.model.enums.NiveauGroupe;
import fr.univ.hyperplanning.model.enums.TypeGroupe;
import fr.univ.hyperplanning.model.gestion.Conflit;
import fr.univ.hyperplanning.model.gestion.Historique;
import fr.univ.hyperplanning.model.utilisateurs.Enseignant;
import fr.univ.hyperplanning.model.utilisateurs.Etudiant;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="groupes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Groupe {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String code;

    @Column(nullable=false)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private NiveauGroupe niveau;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private TypeGroupe typeGroupe;

    @Column(nullable=false)
    private int effectifMax;

    @Column(nullable=false)
    private int effectifActuel;

    @Column(nullable=false)
    private int anneeUniversitaire;


    @ManyToOne(optional = false)
    @JoinColumn(name = "syllabus_id", nullable = false)
    private SyllabusSemestre syllabus;

    @ManyToMany(mappedBy = "groupes")
    private Set<Etudiant> etudiants = new HashSet<>();

    @ManyToMany(mappedBy = "groupesEncadres")
    private Set<Enseignant> enseignants = new HashSet<>();

    @ManyToMany(mappedBy = "groupes")
    private Set<Cours> cours = new HashSet<>();

    @OneToMany(mappedBy = "groupe")
    private Set<Historique> historiques = new HashSet<>();

    @OneToMany(mappedBy = "sourceGroupe")
    private Set<Conflit> conflitsSources = new HashSet<>();

}