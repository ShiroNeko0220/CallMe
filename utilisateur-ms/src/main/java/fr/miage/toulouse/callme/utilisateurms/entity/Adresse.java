package fr.miage.toulouse.callme.utilisateurms.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Embeddable
public class Adresse {
    private String ville;

    private String pays;
}