package fr.miage.toulouse.callme.competitionms.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultatRequest {

    @NotBlank
    private String competitionId;

    @NotNull
    private Long eleveId;

    @NotNull
    private Long enseignantId;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private BigDecimal note;
}
