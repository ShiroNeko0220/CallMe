package fr.miage.toulouse.callme.presencems.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "presence", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_porteur", "id_cours"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Presence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPresence;

    @Column(name = "id_badge", nullable = false)
    private Long idBadge;


    @Column(name = "id_porteur", nullable = false)
    private Long idPorteur;

    @Column(name = "id_cours", nullable = false)
    private Long idCours;

    @Column(name = "date_badgeage", nullable = false)
    private LocalDateTime dateBadgeage;
}
