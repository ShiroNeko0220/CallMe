package fr.miage.toulouse.callme.coursms.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "utilisateur-ms")
public interface UtilisateurClient {

    @GetMapping("/utilisateurs/{id}/apte")
    Boolean enseignantApte(@PathVariable("id") Long id, @RequestParam("niveau") int niveau);
}
