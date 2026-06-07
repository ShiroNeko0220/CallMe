package fr.miage.toulouse.callme.badgesms.controller;

import fr.miage.toulouse.callme.badgesms.DTO.BadgeRequest;
import fr.miage.toulouse.callme.badgesms.entity.Badge;
import fr.miage.toulouse.callme.badgesms.service.BadgeService;
import fr.miage.toulouse.callme.libcommun.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/badges")
public class BadgeController {
    private final BadgeService service;

    public BadgeController(BadgeService service) {
        this.service = service;
    }

    @PostMapping
    public Badge creer(@RequestHeader(value = "X-UserId", required = false) Long userId,
                       @Valid @RequestBody BadgeRequest badgeRequest) {
        if (userId == null) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ID utilisateur manquant: header X-UserId requis");
        }
        return service.creerBadge(userId, badgeRequest);
    }

    @GetMapping
    public List<Badge> lister() {
        return service.listerBadges();
    }

    @GetMapping("/{id}")
    public Badge consulter(@PathVariable Long id) {
        return service.getBadgeById(id);
    }

    @PatchMapping("/{idBadge}/associer/{idPorteur}")
    public Badge associer(@RequestHeader(value = "X-UserId", required = false) Long userId,
                          @PathVariable Long idBadge,
                          @PathVariable Long idPorteur) {
        if (userId == null) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ID utilisateur manquant: header X-UserId requis");
        }
        return service.associerBadge(userId, idBadge, idPorteur);
    }

    @PatchMapping("/{idBadge}/dissocier")
    public Badge dissocier(@RequestHeader(value = "X-UserId", required = false) Long userId,
                           @PathVariable Long idBadge) {
        if (userId == null) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ID utilisateur manquant: header X-UserId requis");
        }
        return service.dissocierBadge(userId, idBadge);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@RequestHeader(value = "X-UserId", required = false) Long userId,
                          @PathVariable Long id) {
        if (userId == null) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ID utilisateur manquant: header X-UserId requis");
        }
        service.supprimerBadge(userId, id);
    }
}

