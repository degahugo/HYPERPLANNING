package fr.univ.hyperplanning.model.utilisateurs;

import fr.univ.hyperplanning.model.enums.RoleUtilisateur;
import fr.univ.hyperplanning.model.gestion.Absence;
import fr.univ.hyperplanning.model.gestion.Historique;
import fr.univ.hyperplanning.model.gestion.Notification;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.sql.Date;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "utilisateurs")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String motDePasseHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleUtilisateur role;

    @Column(nullable = false)
    private Date dateCreation;

    @Column(nullable = false)
    private Date derniereConnexion;

    @Column(nullable = false)
    private boolean actif;

    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DonneePersonnelle donneePersonnelle;

    @OneToMany(mappedBy = "destinataire", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Notification> notifications = new HashSet<>();

    @OneToMany(mappedBy = "effectuePar", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Historique> historiques = new HashSet<>();

    @OneToMany(mappedBy = "declareePar")
    private java.util.Set<Absence> absencesDeclarees = new java.util.HashSet<>();
}
