package fr.miage.toulouse.callme.utilisateurms.DTO;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUtilisateurRequest {
    private String nom;
    private String prenom;

    @Email
    private String email;

    private String ville;
    private String pays;
}
