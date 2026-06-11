package fr.miage.toulouse.callme.coursms.DTO;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class CoursResponse {
    private Long id;
    private String titre;
    private LocalDate date;
    private LocalTime heureDebut;
    private int duree;
    private String lieu;
    private int niveauCible;
    private Long enseignantId;
}