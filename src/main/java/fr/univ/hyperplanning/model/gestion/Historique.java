package fr.univ.hyperplanning.model.gestion;

import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.pedagogie.Groupe;
import fr.univ.hyperplanning.model.pedagogie.Salle;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;

@Entity
@Table(name = "historiques")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Historique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String entiteModifiee;

    @Column(nullable=false)
    private String typeAction;

    @Column(nullable=false)
    private String valeurAvant;

    @Column(nullable=false)
    private String valeurApres;

    @Column(nullable=false)
    private Date dateModification;

    @Column(nullable=false)
    private String adresseIP;

    @ManyToOne(optional = false)
    @JoinColumn(name = "effectue_par_id", nullable = false)
    private Utilisateur effectuePar;

    @ManyToOne
    @JoinColumn(name = "cours_id")
    private Cours cours;

    @ManyToOne
    @JoinColumn(name = "salle_id")
    private Salle salle;

    @ManyToOne
    @JoinColumn(name = "groupe_id")
    private Groupe groupe;

}
