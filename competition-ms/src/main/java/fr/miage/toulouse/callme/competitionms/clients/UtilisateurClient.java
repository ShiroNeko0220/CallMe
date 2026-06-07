package fr.miage.toulouse.callme.competitionms.clients;

import fr.miage.toulouse.callme.libcommun.Role;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "utilisateur-ms", url = "${services.utilisateur.url}")
public interface UtilisateurClient {

    @GetMapping("/utilisateurs/{id}/apte")
    Boolean enseignantApte(
            @PathVariable("id") Long id,
            @RequestParam("niveau") int niveau
    );

    @GetMapping("/utilisateurs/{id}/niveau")
    Integer getNiveauUtilisateur(@PathVariable("id") Long id);

    @GetMapping("/utilisateurs/{id}/role")
    Role getRoleUtilisateur(@PathVariable("id") Long id);
}
