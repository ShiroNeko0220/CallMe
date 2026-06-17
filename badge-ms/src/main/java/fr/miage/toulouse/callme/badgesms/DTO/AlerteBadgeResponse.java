package fr.miage.toulouse.callme.badgesms.DTO;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Builder
public class AlerteBadgeResponse {
    private Long id;
    private String typeActivite;
    private String idActivite;
    private String titreActivite;
    private LocalDate dateActivite;
    private Long idEnseignant;
    private LocalDateTime dateCreation;
    private boolean resolue;
}