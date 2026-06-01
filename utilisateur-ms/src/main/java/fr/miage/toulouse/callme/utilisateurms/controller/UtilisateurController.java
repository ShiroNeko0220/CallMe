package fr.miage.toulouse.callme.utilisateurms.controller;

import fr.miage.toulouse.callme.libcommun.*;
import fr.miage.toulouse.callme.utilisateurms.DTO.UtilisateurCreationRequest;
import fr.miage.toulouse.callme.utilisateurms.service.UtilisateurService;
import fr.miage.toulouse.callme.utilisateurms.entity.Utilisateur;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/utilisateurs")
public class UtilisateurController {
    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service){
        this.service=service;
    }

    @PostMapping
    public Utilisateur creer(@Valid @RequestBody UtilisateurCreationRequest u){
        return service.creer(u);
    }

    @GetMapping("/{id}")
    public Utilisateur consulter(@PathVariable Long id){
        return service.consulter(id);
    }

    @GetMapping
    public List<Utilisateur> lister(){
        return service.lister();
    }

    @PatchMapping("/{id}")
    public Utilisateur modifier(@RequestHeader(value="X-Role",required=false) String role,@RequestBody Utilisateur u){
        RoleCheck.require(role, Role.SECRETAIRE, Role.PRESIDENT);
        return service.modifier(u.getId(),u);
    }

    @GetMapping("/{id}/apte")
    public boolean enseignantApte(@PathVariable Long id,@RequestParam int niveau){
        return service.enseignantApte(id,niveau);
    }

    @GetMapping("/{id}/niveau")
    public Integer getNiveauUtilisateur(@PathVariable Long id) {
        return service.getNiveauUtilisateur(id);
    }

    @GetMapping("/{id}/role")
    public Role getRoleUtilisateur(@PathVariable Long id) {
        return service.getRoleUtilisateur(id);
    }
}
