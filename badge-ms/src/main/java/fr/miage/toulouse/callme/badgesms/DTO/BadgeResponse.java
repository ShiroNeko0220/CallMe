package fr.miage.toulouse.callme.badgesms.DTO;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class BadgeResponse {
    private Long idBadge;
    private Long idPorteur;
    private String statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateAssociation;
}