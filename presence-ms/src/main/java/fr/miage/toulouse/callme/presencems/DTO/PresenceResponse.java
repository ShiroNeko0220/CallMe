package fr.miage.toulouse.callme.presencems.DTO;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PresenceResponse {
    private Long idPresence;
    private Long idBadge;
    private Long idPorteur;
    private Long idCours;
    private LocalDateTime dateBadgeage;
}
