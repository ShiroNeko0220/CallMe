package fr.miage.toulouse.callme.statistiquesms.controller;

import fr.miage.toulouse.callme.statistiquesms.DTO.*;
import fr.miage.toulouse.callme.statistiquesms.clients.*;
import fr.miage.toulouse.callme.statistiquesms.service.StatistiquesService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/statistiques")
public class StatistiquesController {

    private final StatistiquesService service;

    public StatistiquesController(StatistiquesService service) {
        this.service = service;
    }

    @GetMapping("/cours")
    @PreAuthorize("hasRole('PRESIDENT')")
    public CoursStatistiqueResponse statistiquesCours() {
        return service.statistiquesCours();
    }

    @GetMapping("/cours/{idCours}/eleves")
    @PreAuthorize("hasRole('PRESIDENT')")
    public List<PresenceClient.PresenceResponse> elevesPresentsCours(@PathVariable Long idCours) {
        return service.elevesPresentsCours(idCours);
    }

    @GetMapping("/eleves/{eleveId}/cours")
    @PreAuthorize("hasRole('PRESIDENT')")
    public List<CoursPresenceEleveResponse> coursPourEleve(
            @PathVariable Long eleveId,
            @RequestParam(value = "debut", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(value = "fin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return service.coursPourEleveAvecPresence(eleveId, debut, fin);
    }

    @GetMapping("/competitions/niveau/{niveau}/nombre")
    @PreAuthorize("hasRole('PRESIDENT')")
    public Long nombreCompetitionsParNiveau(@PathVariable int niveau) {
        return service.nombreCompetitionsParNiveau(niveau);
    }

    @GetMapping("/eleves/{eleveId}/competitions")
    @PreAuthorize("hasRole('PRESIDENT')")
    public List<CompetitionClient.ResultatResponse> resultatsCompetitionEleve(
            @PathVariable Long eleveId,
            @RequestParam(value = "debut", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(value = "fin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return service.resultatsCompetitionEleve(eleveId, debut, fin);
    }
}
