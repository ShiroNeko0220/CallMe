package fr.miage.toulouse.callme.utilisateurms.DTO;

import fr.miage.toulouse.callme.utilisateurms.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Min(1)
    @Max(5)
    private Integer niveauExpertise;

    private Role role;
}
