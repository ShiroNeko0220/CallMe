package fr.miage.toulouse.callme.statistiquesms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "stat_competition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatCompetition {
    @Id
    private String id;
    private String titre;
    private Integer niveauCible;
    private LocalDate date;
}
