package fr.univ.hyperplanning.model.pedagogie;

import fr.univ.hyperplanning.model.enums.NiveauGroupe;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "syllabus_semestres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusSemestre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NiveauGroupe niveau;

    @Column(nullable = false)
    private int semestre;

    @Column(nullable = false)
    private String anneeUniversitaire;

    @Column(nullable=false)
    private Date dateDebut;

    @Column(nullable=false)
    private Date dateFin;

    @OneToMany(mappedBy = "syllabus", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ModuleCours> modules = new HashSet<>();

    @OneToMany(mappedBy = "syllabus")
    private Set<Groupe> groupes = new HashSet<>();
}
