package fr.miage.toulouse.callme.utilisateurms.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String login;
    @NotBlank
    private String mdp;
}
