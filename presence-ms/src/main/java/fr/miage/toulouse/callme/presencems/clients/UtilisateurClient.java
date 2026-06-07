package fr.miage.toulouse.callme.presencems.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "utilisateur-ms-presence", url = "${services.utilisateur.url}")
public interface UtilisateurClient {
    @GetMapping("/utilisateurs/{id}/niveau")
    Integer getNiveauUtilisateur(@PathVariable("id") Long id);
}
