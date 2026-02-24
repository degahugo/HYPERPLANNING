package fr.univ.hyperplanning.model.utilisateurs;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;

@Entity
@Table(name = "donnees_personnelles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonneePersonnelle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String adresse;

    @Column(nullable = false)
    private String telephone;

    @Column(nullable = false)
    private Date dateNaissance;

    @Column(nullable = false)
    private String lieuNaissance;

    @Column(nullable = false)
    private String photoUrl;

    @OneToOne(optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private Utilisateur utilisateur;
}
