package fr.miage.toulouse.callme.competitionms.entity;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "resultats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Resultat {

    @Id
    private String id;

    @NotNull
    private String competitionId;

    @NotNull
    private Long eleveId;

    @NotNull
    private Long enseignantId;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private BigDecimal note;

    private LocalDate competitionDate;
}
