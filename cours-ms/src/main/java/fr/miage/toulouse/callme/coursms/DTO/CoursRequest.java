package fr.miage.toulouse.callme.coursms.DTO;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.*;


@Getter
@Setter
public class CoursRequest {

    @NotBlank
    private String titre;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime heureDebut;

    @Min(45)
    private Integer duree;

    private String lieu;

    @Min(1)
    @Max(5)
    private Integer niveauCible;

    @NotNull
    private Long enseignantId;
}
