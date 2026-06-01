package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.*;

@Entity
@Table(name = "competitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String titre;

    @Min(1)
    @Max(5)
    @Column(name = "niveau_cible", nullable = false)
    private int niveauCible;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    @NotNull
    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Min(45)
    @Column(name = "duree_minutes", nullable = false)
    private int duree;

    private String lieu;

    @NotNull
    @Column(name = "enseignant_id", nullable = false)
    private Long enseignantId;
}
