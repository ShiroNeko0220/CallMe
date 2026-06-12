package fr.miage.toulouse.callme.statistiquesms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stat_presence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatPresence {
    @Id
    private Long id;
    private Long idPorteur;
    private Long idCours;
    private LocalDateTime dateBadgeage;
}
