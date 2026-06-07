package fr.miage.toulouse.callme.badgesms.DTO;

import jakarta.validation.constraints.NotNull;

public class AssocierBadgeRequest {

    @NotNull
    private Long idPorteur;

    public Long getIdPorteur() {
        return idPorteur;
    }

    public void setIdPorteur(Long idPorteur) {
        this.idPorteur = idPorteur;
    }
}