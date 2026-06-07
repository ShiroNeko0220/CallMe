package fr.miage.toulouse.callme.presencems.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadgeageRequest {
    @NotNull
    private Long idBadge;

    @NotNull
    private Long idCours;
}
