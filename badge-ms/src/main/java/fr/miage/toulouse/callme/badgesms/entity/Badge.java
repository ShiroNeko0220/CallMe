package fr.miage.toulouse.callme.badgesms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "badge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBadge;

    private Long idPorteur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut;

    private LocalDateTime dateCreation;

    private LocalDateTime dateAssociation;

}
