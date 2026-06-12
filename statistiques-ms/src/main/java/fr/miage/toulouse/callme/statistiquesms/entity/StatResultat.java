package fr.miage.toulouse.callme.statistiquesms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "stat_resultat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatResultat {
    @Id
    private String id;
    private String competitionId;
    private Long eleveId;
    private Long enseignantId;
    private BigDecimal note;
    private LocalDate competitionDate;
}
