package fr.miage.toulouse.callme.utilisateurms.DTO;

import fr.miage.toulouse.callme.utilisateurms.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UtilisateurResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String login;
    private String ville;
    private String pays;
    private int niveauExpertise;
    private Role role;
}
