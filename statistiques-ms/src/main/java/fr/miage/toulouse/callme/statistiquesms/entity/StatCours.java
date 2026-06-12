package fr.miage.toulouse.callme.statistiquesms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "stat_cours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatCours {
    @Id
    private Long id;
    private String titre;
    private Integer niveauCible;
    private LocalDate date;
    private LocalTime heureDebut;
    private Integer duree;
    private Long enseignantId;
}
