package fr.miage.toulouse.callme.statistiquesms.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "utilisateur-ms", contextId = "utilisateurStatistiquesClient")
public interface UtilisateurClient {
    @GetMapping("/utilisateurs/{id}/niveau")
    Integer getNiveauUtilisateur(@PathVariable("id") Long id);
}
