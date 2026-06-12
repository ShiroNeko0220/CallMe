package fr.miage.toulouse.callme.statistiquesms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stat_eleve")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatEleve {
    @Id
    private Long id;
    private Integer niveauExpertise;
}
