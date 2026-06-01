package fr.miage.toulouse.callme.utilisateurms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Embeddable
public class IdConnexion {
    @NotBlank
    @Column(name = "login", unique = true, nullable = false)
    private String login;

    @NotBlank
    @Column(name = "mot_de_passe", nullable = false)
    private String mdp;
}