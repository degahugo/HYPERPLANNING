package fr.univ.hyperplanning.model.pedagogie;

import fr.univ.hyperplanning.model.enums.TypeSalle;
import fr.univ.hyperplanning.model.gestion.Conflit;
import fr.univ.hyperplanning.model.gestion.Historique;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="salles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Salle {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String numero;

    @Column(nullable=false)
    private String batiment;

    @Column(nullable=false)
    private int capacite;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private TypeSalle typeSalle;

    @Column(nullable=false)
    private boolean actif;

    @OneToMany(mappedBy = "salle")
    private Set<Cours> cours = new HashSet<>();

    @OneToMany(mappedBy = "salle")
    private Set<Historique> historiques = new HashSet<>();

    @OneToMany(mappedBy = "sourceSalle")
    private Set<Conflit> conflitsSources = new HashSet<>();
}