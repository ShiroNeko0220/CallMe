package fr.miage.toulouse.callme.badgesms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerte_badge")
@Getter @Setter @NoArgsConstructor
public class AlerteBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String typeActivite; // "COURS" ou "COMPETITION"

    @Column(nullable = false)
    private String idActivite;

    @Column(nullable = false)
    private String titreActivite;

    private LocalDate dateActivite;

    @Column(nullable = false)
    private Long idEnseignant;

    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private boolean resolue = false;
}
