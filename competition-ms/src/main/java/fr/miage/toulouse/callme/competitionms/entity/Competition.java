package fr.miage.toulouse.callme.competitionms.entity;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.*;

@Document(collection = "competitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Competition {

    @Id
    private String id;

    @NotBlank
    private String titre;

    @Min(1)
    @Max(5)
    private int niveauCible;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime heureDebut;

    @Min(45)
    private int duree;

    private String lieu;

    @NotNull
    private Long enseignantId;
}
