package fr.miage.toulouse.callme.badgesms.controller;

import fr.miage.toulouse.callme.badgesms.DTO.BadgeRequest;
import fr.miage.toulouse.callme.badgesms.DTO.BadgeResponse;
import fr.miage.toulouse.callme.badgesms.service.BadgeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/badges")
public class BadgeController {

    private final BadgeService service;

    public BadgeController(BadgeService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SECRETAIRE', 'PRESIDENT')")
    public BadgeResponse creer(@Valid @RequestBody BadgeRequest badgeRequest) {
        return service.creerBadge(badgeRequest);
    }

    @GetMapping
    public List<BadgeResponse> lister() {
        return service.listerBadges();
    }

    @GetMapping("/{id}")
    public BadgeResponse consulter(@PathVariable Long id) {
        return service.getBadgeById(id);
    }

    @PatchMapping("/{idBadge}/associer/{idPorteur}")
    @PreAuthorize("hasAnyRole('SECRETAIRE', 'PRESIDENT')")
    public BadgeResponse associer(@PathVariable Long idBadge, @PathVariable Long idPorteur) {
        return service.associerBadge(idBadge, idPorteur);
    }

    @PatchMapping("/{idBadge}/dissocier")
    @PreAuthorize("hasAnyRole('SECRETAIRE', 'PRESIDENT')")
    public BadgeResponse dissocier(@PathVariable Long idBadge) {
        return service.dissocierBadge(idBadge);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimerBadge(id);
        return ResponseEntity.noContent().build();
    }
}
