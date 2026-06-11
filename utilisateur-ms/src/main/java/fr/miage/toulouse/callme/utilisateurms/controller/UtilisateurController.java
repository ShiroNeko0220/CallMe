package fr.miage.toulouse.callme.utilisateurms.controller;

import fr.miage.toulouse.callme.utilisateurms.DTO.LoginRequest;
import fr.miage.toulouse.callme.utilisateurms.DTO.UpdateUtilisateurRequest;
import fr.miage.toulouse.callme.utilisateurms.DTO.UtilisateurCreationRequest;
import fr.miage.toulouse.callme.utilisateurms.DTO.UtilisateurResponse;
import fr.miage.toulouse.callme.utilisateurms.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public UtilisateurResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request.getLogin(), request.getMdp());
    }

    @PostMapping
    public UtilisateurResponse creer(@Valid @RequestBody UtilisateurCreationRequest u) {
        return service.creer(u);
    }

    @GetMapping("/{id}")
    public UtilisateurResponse consulter(@PathVariable Long id) {
        return service.consulter(id);
    }

    @GetMapping
    public List<UtilisateurResponse> lister() {
        return service.lister();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETAIRE', 'PRESIDENT')")
    public UtilisateurResponse modifier(
            @PathVariable Long id,
            @RequestBody UpdateUtilisateurRequest request) {
        return service.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/apte")
    public boolean enseignantApte(@PathVariable Long id, @RequestParam int niveau) {
        return service.enseignantApte(id, niveau);
    }

    @GetMapping("/{id}/niveau")
    public Integer getNiveauUtilisateur(@PathVariable Long id) {
        return service.getNiveauUtilisateur(id);
    }

    @GetMapping("/{id}/role")
    public String getRoleUtilisateur(@PathVariable Long id) {
        return service.getRoleUtilisateur(id).name();
    }

    @GetMapping("/{id}/exists")
    public Boolean existsById(@PathVariable Long id) {
        return service.existsById(id);
    }
}
