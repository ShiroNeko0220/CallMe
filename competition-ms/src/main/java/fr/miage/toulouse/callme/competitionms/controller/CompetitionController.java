package fr.miage.toulouse.callme.competitionms.controller;

import fr.miage.toulouse.callme.competitionms.DTO.CompetitionRequest;
import fr.miage.toulouse.callme.competitionms.DTO.CompetitionResponse;
import fr.miage.toulouse.callme.competitionms.DTO.ResultatRequest;
import fr.miage.toulouse.callme.competitionms.DTO.ResultatResponse;
import fr.miage.toulouse.callme.competitionms.service.CompetitionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'PRESIDENT')")
    public CompetitionResponse creer(@Valid @RequestBody CompetitionRequest request) {
        return competitionService.creer(request);
    }

    @GetMapping("/{id}")
    public CompetitionResponse consulter(@PathVariable String id) {
        return competitionService.consulter(id);
    }

    @GetMapping
    public List<CompetitionResponse> lister() {
        return competitionService.lister();
    }

    @GetMapping("/niveau/{niveau}")
    public List<CompetitionResponse> listerParNiveau(@PathVariable int niveau) {
        return competitionService.listerParNiveau(niveau);
    }

    @GetMapping("/enseignant/{enseignantId}")
    public List<CompetitionResponse> listerParEnseignant(@PathVariable Long enseignantId) {
        return competitionService.listerParEnseignant(enseignantId);
    }

    @GetMapping("/eleve/{eleveId}")
    public List<CompetitionResponse> listerPourEleve(@PathVariable Long eleveId) {
        return competitionService.listerPourEleve(eleveId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public ResponseEntity<Void> supprimer(@PathVariable String id) {
        competitionService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{competitionId}/resultats")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'PRESIDENT')")
    public ResultatResponse ajouterResultat(
            @PathVariable String competitionId,
            @Valid @RequestBody ResultatRequest request) {
        return competitionService.ajouterResultat(competitionId, request);
    }

    @GetMapping("/{competitionId}/resultats")
    public List<ResultatResponse> listerResultatsParCompetition(@PathVariable String competitionId) {
        return competitionService.listerResultatsParCompetition(competitionId);
    }

    @GetMapping("/eleve/{eleveId}/resultats")
    public List<ResultatResponse> listerResultatsPourEleve(
            @PathVariable Long eleveId,
            @RequestParam(value = "debut", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(value = "fin", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return competitionService.listerResultatsPourEleveSurPeriode(eleveId, debut, fin);
    }

    @GetMapping("/niveau/{niveau}/count")
    public long compterParNiveau(@PathVariable int niveau) {
        return competitionService.compterParNiveau(niveau);
    }
}
