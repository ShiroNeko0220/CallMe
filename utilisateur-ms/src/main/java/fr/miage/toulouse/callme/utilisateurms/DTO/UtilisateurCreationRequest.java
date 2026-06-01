package fr.miage.toulouse.callme.utilisateurms.DTO;

import fr.miage.toulouse.callme.utilisateurms.entity.Adresse;
import fr.miage.toulouse.callme.utilisateurms.entity.IdConnexion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class UtilisateurCreationRequest {
    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;

    @Email
    private String email;

    @Valid
    private IdConnexion idConnexion;

    @Valid
    private Adresse adresse;

}
