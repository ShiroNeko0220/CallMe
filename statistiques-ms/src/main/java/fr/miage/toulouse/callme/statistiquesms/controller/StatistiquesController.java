package fr.miage.toulouse.callme.statistiquesms.controller;

import fr.miage.toulouse.callme.libcommun.*;
import fr.miage.toulouse.callme.statistiquesms.DTO.*;
import fr.miage.toulouse.callme.statistiquesms.clients.*;
import fr.miage.toulouse.callme.statistiquesms.service.StatistiquesService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/statistiques")
public class StatistiquesController {
    private final StatistiquesService service;

    public StatistiquesController(StatistiquesService service) { this.service = service; }

    @GetMapping("/cours")
    public CoursStatistiqueResponse statistiquesCours(@RequestHeader(value = "X-Role", required = false) String role) {
        RoleCheck.require(role, Role.PRESIDENT);
        return service.statistiquesCours();
    }

    @GetMapping("/cours/{idCours}/eleves")
    public List<PresenceClient.PresenceResponse> elevesPresentsCours(@RequestHeader(value = "X-Role", required = false) String role,
                                                                     @PathVariable Long idCours) {
        RoleCheck.require(role, Role.PRESIDENT);
        return service.elevesPresentsCours(idCours);
    }

    @GetMapping("/eleves/{eleveId}/cours")
    public List<CoursPresenceEleveResponse> coursPourEleve(@RequestHeader(value = "X-Role", required = false) String role,
                                                           @PathVariable Long eleveId,
                                                           @RequestParam(value = "debut", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
                                                           @RequestParam(value = "fin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        RoleCheck.require(role, Role.PRESIDENT);
        return service.coursPourEleveAvecPresence(eleveId, debut, fin);
    }

    @GetMapping("/competitions/niveau/{niveau}/nombre")
    public Long nombreCompetitionsParNiveau(@RequestHeader(value = "X-Role", required = false) String role,
                                            @PathVariable int niveau) {
        RoleCheck.require(role, Role.PRESIDENT);
        return service.nombreCompetitionsParNiveau(niveau);
    }

    @GetMapping("/eleves/{eleveId}/competitions")
    public List<CompetitionClient.ResultatResponse> resultatsCompetitionEleve(@RequestHeader(value = "X-Role", required = false) String role,
                                                                              @PathVariable Long eleveId,
                                                                              @RequestParam(value = "debut", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
                                                                              @RequestParam(value = "fin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        RoleCheck.require(role, Role.PRESIDENT);
        return service.resultatsCompetitionEleve(eleveId, debut, fin);
    }
}
