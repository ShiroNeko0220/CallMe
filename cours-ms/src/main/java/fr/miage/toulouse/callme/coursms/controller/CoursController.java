package fr.miage.toulouse.callme.coursms.controller;

import fr.miage.toulouse.callme.coursms.DTO.*;
import fr.miage.toulouse.callme.coursms.entity.*;
import fr.miage.toulouse.callme.coursms.service.*;
import fr.miage.toulouse.callme.libcommun.*;
import jakarta.validation.Valid;
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
    public Cours creer(@RequestHeader(value = "X-Role", required = false) String role, @Valid @RequestBody CoursRequest request) {
        RoleCheck.require(role, Role.SECRETAIRE, Role.PRESIDENT);
        return service.creer(request);
    }

    @GetMapping("/{id}")
    public Cours consulter(@PathVariable Long id) {
        return service.consulter(id);
    }

    @GetMapping
    public List<Cours> lister() {
        return service.lister();
    }

    @GetMapping("/enseignant/{enseignantId}")
    public List<Cours> listerParEnseignant(@PathVariable Long enseignantId) {
        return service.listerParEnseignant(enseignantId);
    }

    @GetMapping("/niveau/{niveau}")
    public List<Cours> listerParNiveau(@PathVariable int niveau) {
        return service.listerParNiveau(niveau);
    }

    @PatchMapping("/{id}")
    public Cours modifier(@RequestHeader(value = "X-Role", required = false) String role,
                          @PathVariable Long id,
                          @RequestBody CoursRequest request) {
        RoleCheck.require(role, Role.SECRETAIRE, Role.PRESIDENT);
        return service.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@RequestHeader(value = "X-Role", required = false) String role,
                          @PathVariable Long id) {
        RoleCheck.require(role, Role.PRESIDENT);
        service.supprimer(id);
    }
}