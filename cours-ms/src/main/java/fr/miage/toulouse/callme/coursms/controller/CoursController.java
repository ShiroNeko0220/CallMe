package fr.miage.toulouse.callme.coursms.controller;

import fr.miage.toulouse.callme.coursms.DTO.CoursRequest;
import fr.miage.toulouse.callme.coursms.DTO.CoursResponse;
import fr.miage.toulouse.callme.coursms.service.CoursService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cours")
public class CoursController {

    private final CoursService service;

    public CoursController(CoursService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SECRETAIRE', 'PRESIDENT')")
    public CoursResponse creer(@Valid @RequestBody CoursRequest request) {
        return service.creer(request);
    }

    @GetMapping("/{id}")
    public CoursResponse consulter(@PathVariable Long id) {
        return service.consulter(id);
    }

    @GetMapping
    public List<CoursResponse> lister() {
        return service.lister();
    }

    @GetMapping("/enseignant/{enseignantId}")
    public List<CoursResponse> listerParEnseignant(@PathVariable Long enseignantId) {
        return service.listerParEnseignant(enseignantId);
    }

    @GetMapping("/niveau/{niveau}")
    public List<CoursResponse> listerParNiveau(@PathVariable int niveau) {
        return service.listerParNiveau(niveau);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETAIRE', 'PRESIDENT')")
    public CoursResponse modifier(@PathVariable Long id, @RequestBody CoursRequest request) {
        return service.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }
}
