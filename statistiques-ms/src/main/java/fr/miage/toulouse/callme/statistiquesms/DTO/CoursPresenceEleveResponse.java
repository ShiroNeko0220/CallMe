package fr.miage.toulouse.callme.statistiquesms.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class CoursPresenceEleveResponse {
    private Long idCours;
    private String titre;
    private LocalDate date;
    private LocalTime heureDebut;
    private Integer niveauCible;
    private boolean present;
}
