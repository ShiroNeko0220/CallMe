package fr.miage.toulouse.callme.competitionms.controller;

import fr.miage.toulouse.callme.competitionms.DTO.CompetitionRequest;
import fr.miage.toulouse.callme.competitionms.DTO.ResultatRequest;
import fr.miage.toulouse.callme.competitionms.entity.Competition;
import fr.miage.toulouse.callme.competitionms.entity.Resultat;
import fr.miage.toulouse.callme.competitionms.service.CompetitionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/competitions")
public class CompetitionController {

    private final CompetitionService competitionService;

    public CompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @PostMapping
    public Competition creer(@Valid @RequestBody CompetitionRequest request) {
        return competitionService.creer(request);
    }

    @GetMapping("/{id}")
    public Competition consulter(@PathVariable String id) {
        return competitionService.consulter(id);
    }

    @GetMapping
    public List<Competition> lister() {
        return competitionService.lister();
    }

    @GetMapping("/niveau/{niveau}")
    public List<Competition> listerParNiveau(@PathVariable int niveau) {
        return competitionService.listerParNiveau(niveau);
    }

    @GetMapping("/enseignant/{enseignantId}")
    public List<Competition> listerParEnseignant(@PathVariable Long enseignantId) {
        return competitionService.listerParEnseignant(enseignantId);
    }

    @GetMapping("/eleve/{eleveId}")
    public List<Competition> listerPourEleve(@PathVariable Long eleveId) {
        return competitionService.listerPourEleve(eleveId);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable String id) {
        competitionService.supprimer(id);
    }

    @PostMapping("/resultats")
    public Resultat ajouterResultat(@Valid @RequestBody ResultatRequest request) {
        return competitionService.ajouterResultat(request);
    }

    @GetMapping("/{competitionId}/resultats")
    public List<Resultat> listerResultatsParCompetition(@PathVariable String competitionId) {
        return competitionService.listerResultatsParCompetition(competitionId);
    }

    @GetMapping("/eleve/{eleveId}/resultats")
    public List<Resultat> listerResultatsPourEleve(
            @PathVariable Long eleveId,
            @RequestParam(value = "debut", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(value = "fin", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        return competitionService.listerResultatsPourEleveSurPeriode(eleveId, debut, fin);
    }

    @GetMapping("/niveau/{niveau}/count")
    public long compterParNiveau(@PathVariable int niveau) {
        return competitionService.compterParNiveau(niveau);
    }
}
