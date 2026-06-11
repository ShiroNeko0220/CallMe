package fr.miage.toulouse.callme.presencems.controller;

import fr.miage.toulouse.callme.presencems.DTO.BadgeageRequest;
import fr.miage.toulouse.callme.presencems.DTO.PresenceResponse;
import fr.miage.toulouse.callme.presencems.service.PresenceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/presences")
public class PresenceController {

    private final PresenceService service;

    public PresenceController(PresenceService service) {
        this.service = service;
    }

    @PostMapping("/badger")
    public PresenceResponse badger(@Valid @RequestBody BadgeageRequest request) {
        return service.badger(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SECRETAIRE', 'PRESIDENT')")
    public List<PresenceResponse> lister() {
        return service.lister();
    }

    @GetMapping("/eleve/{idEleve}")
    public List<PresenceResponse> listerParEleve(
            @PathVariable Long idEleve,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return service.listerParEleve(idEleve, debut, fin);
    }

    @GetMapping("/cours/{idCours}")
    public List<PresenceResponse> listerParCours(@PathVariable Long idCours) {
        return service.listerParCours(idCours);
    }

    @GetMapping("/cours/{idCours}/count")
    public long compterParCours(@PathVariable Long idCours) {
        return service.compterParCours(idCours);
    }

    @GetMapping("/cours/counts")
    public Map<Long, Long> compterParTousCours() {
        return service.compterParTousCours();
    }
}
