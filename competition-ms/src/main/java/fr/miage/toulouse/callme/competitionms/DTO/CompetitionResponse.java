package fr.miage.toulouse.callme.competitionms.DTO;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class CompetitionResponse {
    private String id;
    private String titre;
    private int niveauCible;
    private LocalDate date;
    private LocalTime heureDebut;
    private int duree;
    private String lieu;
    private Long enseignantId;
}
