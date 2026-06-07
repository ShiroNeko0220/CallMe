package fr.miage.toulouse.callme.badgesms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "presence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Presence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPresence;

    @Column(nullable = false)
    private Long idBadge;

    @Column(nullable = false)
    private Long idPorteur;

    @Column(nullable = false)
    private Long idCours;

    @Column(nullable = false)
    private LocalDateTime dateBadgeage;
}
