package fr.miage.toulouse.callme.competitionms.DTO;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ResultatResponse {
    private String id;
    private String competitionId;
    private Long eleveId;
    private Long enseignantId;
    private BigDecimal note;
    private LocalDate competitionDate;
}
