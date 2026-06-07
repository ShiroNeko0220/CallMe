package fr.miage.toulouse.callme.competitionms.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionRequest {

    @NotBlank
    private String titre;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer niveauCible;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime heureDebut;

    @NotNull
    @Min(45)
    private Integer duree;

    private String lieu;

    @NotNull
    private Long enseignantId;
}
