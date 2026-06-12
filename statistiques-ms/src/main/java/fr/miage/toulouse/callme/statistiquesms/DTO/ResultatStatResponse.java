package fr.miage.toulouse.callme.statistiquesms.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ResultatStatResponse {
    private String id;
    private String competitionId;
    private Long eleveId;
    private Long enseignantId;
    private BigDecimal note;
    private LocalDate competitionDate;
}
