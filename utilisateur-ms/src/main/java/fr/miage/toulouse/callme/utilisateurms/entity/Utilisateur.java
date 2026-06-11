package fr.miage.toulouse.callme.utilisateurms.entity;

import fr.miage.toulouse.callme.utilisateurms.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "utilisateur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;

    @Email
    @Column(unique = true)
    private String email;

    @Valid
    @Embedded
    @Column(unique = true)
    private IdConnexion idConnexion;

    @Valid
    @Embedded
    private Adresse adresse;

    @Min(1)
    @Max(5)
    @Column(name = "niveau_expertise", nullable = false)
    private int niveauExpertise;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;
}
