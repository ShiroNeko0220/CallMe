package fr.miage.toulouse.callme.statistiquesms.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PresenceStatResponse {
    private Long id;
    private Long idPorteur;
    private Long idCours;
    private LocalDateTime dateBadgeage;
}
