package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "resultats_competition", uniqueConstraints = { @UniqueConstraint(columnNames = {"competition_id", "eleve_id"}) })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Resultat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @NotNull
    @Column(name = "eleve_id", nullable = false)
    private Long eleveId;

    @NotNull
    @Column(name = "enseignant_id", nullable = false)
    private Long enseignantId;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("10.0")
    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal note;
}
