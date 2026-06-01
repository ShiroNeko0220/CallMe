package fr.miage.toulouse.callme.coursms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.*;


@Entity
@Table(name = "cours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String titre;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @NotNull
    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Min(45)
    @NotNull
    @Column(name = "duree_minutes", nullable = false)
    private int duree;

    private String lieu;

    @Min(1)
    @Max(5)
    @Column(name = "niveau_cible", nullable = false)
    private int niveauCible;

    @NotNull
    @Column(name = "enseignant_id", nullable = false)
    private Long enseignantId;

}