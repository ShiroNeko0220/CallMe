package fr.miage.toulouse.callme.presencems.controller;

import fr.miage.toulouse.callme.libcommun.*;
import fr.miage.toulouse.callme.presencems.DTO.BadgeageRequest;
import fr.miage.toulouse.callme.presencems.entity.Presence;
import fr.miage.toulouse.callme.presencems.service.PresenceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/presences")
public class PresenceController {
    private final PresenceService service;

    public PresenceController(PresenceService service) { this.service = service; }

    /** Simulation du boîtier de badgeage par appel REST. */
    @PostMapping("/badger")
    public Presence badger(@Valid @RequestBody BadgeageRequest request) { return service.badger(request); }

    @GetMapping
    public List<Presence> lister(@RequestHeader(value = "X-Role", required = false) String role) {
        RoleCheck.require(role, Role.SECRETAIRE, Role.PRESIDENT);
        return service.lister();
    }

    @GetMapping("/eleve/{idEleve}")
    public List<Presence> listerParEleve(@PathVariable Long idEleve,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return service.listerParEleve(idEleve, debut, fin);
    }

    @GetMapping("/cours/{idCours}")
    public List<Presence> listerParCours(@PathVariable Long idCours) { return service.listerParCours(idCours); }

    @GetMapping("/cours/{idCours}/count")
    public long compterParCours(@PathVariable Long idCours) { return service.compterParCours(idCours); }
}
