package fr.univ.hyperplanning.model.gestion;

import fr.univ.hyperplanning.model.pedagogie.Cours;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String titre;

    @Column(nullable=false)
    private String message;

    @Column(nullable=false)
    private Date dateEnvoi;

    @Column(nullable=false)
    private Date dateLecture;

    @Column(nullable=false)
    private boolean estLue;

    @Column(nullable=false)
    private String type;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Utilisateur destinataire;

    @ManyToOne
    @JoinColumn(name = "cours_id")
    private Cours cours;

    @ManyToOne
    @JoinColumn(name = "absence_id")
    private Absence absence;

    @ManyToOne
    @JoinColumn(name = "demande_modification_id")
    private DemandeModification demandeModification;

}
