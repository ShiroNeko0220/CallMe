package fr.miage.toulouse.callme.statistiquesms.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CoursStatistiqueResponse {
    private long nombreCours;
    private double nombreMoyenElevesPresents;
}
