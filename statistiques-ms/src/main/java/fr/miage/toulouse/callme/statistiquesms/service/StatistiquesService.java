package fr.miage.toulouse.callme.statistiquesms.service;

import fr.miage.toulouse.callme.libcommun.ApiException;
import fr.miage.toulouse.callme.statistiquesms.DTO.*;
import fr.miage.toulouse.callme.statistiquesms.entity.*;
import fr.miage.toulouse.callme.statistiquesms.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StatistiquesService {

    private final StatCoursRepository coursRepo;
    private final StatPresenceRepository presenceRepo;
    private final StatCompetitionRepository competitionRepo;
    private final StatResultatRepository resultatRepo;
    private final StatEleveRepository eleveRepo;

    public StatistiquesService(StatCoursRepository coursRepo,
                               StatPresenceRepository presenceRepo,
                               StatCompetitionRepository competitionRepo,
                               StatResultatRepository resultatRepo,
                               StatEleveRepository eleveRepo) {
        this.coursRepo = coursRepo;
        this.presenceRepo = presenceRepo;
        this.competitionRepo = competitionRepo;
        this.resultatRepo = resultatRepo;
        this.eleveRepo = eleveRepo;
    }

    public CoursStatistiqueResponse statistiquesCours() {
        List<StatCours> cours = coursRepo.findAll();
        long nombreCours = cours.size();
        if (nombreCours == 0) return new CoursStatistiqueResponse(0, 0.0);

        long totalPresences = cours.stream()
                .mapToLong(c -> presenceRepo.countByIdCours(c.getId()))
                .sum();
        return new CoursStatistiqueResponse(nombreCours, (double) totalPresences / nombreCours);
    }

    public List<PresenceStatResponse> elevesPresentsCours(Long idCours) {
        return presenceRepo.findByIdCours(idCours).stream()
                .map(p -> new PresenceStatResponse(p.getId(), p.getIdPorteur(), p.getIdCours(), p.getDateBadgeage()))
                .toList();
    }

    public List<CoursPresenceEleveResponse> coursPourEleveAvecPresence(Long eleveId, LocalDate debut, LocalDate fin) {
        StatEleve eleve = eleveRepo.findById(eleveId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Élève introuvable"));

        List<StatCours> coursNiveau = coursRepo.findByNiveauCible(eleve.getNiveauExpertise());

        List<StatPresence> presences = (debut != null && fin != null)
                ? presenceRepo.findByIdPorteurAndDateBadgeageBetween(eleveId, debut.atStartOfDay(), fin.atTime(LocalTime.MAX))
                : presenceRepo.findByIdPorteur(eleveId);

        Set<Long> coursPresents = presences.stream().map(StatPresence::getIdCours).collect(Collectors.toSet());

        return coursNiveau.stream()
                .filter(c -> debut == null || !c.getDate().isBefore(debut))
                .filter(c -> fin == null || !c.getDate().isAfter(fin))
                .map(c -> new CoursPresenceEleveResponse(c.getId(), c.getTitre(), c.getDate(), c.getHeureDebut(), c.getNiveauCible(), coursPresents.contains(c.getId())))
                .toList();
    }

    public Long nombreCompetitionsParNiveau(int niveau) {
        return competitionRepo.countByNiveauCible(niveau);
    }

    public List<ResultatStatResponse> resultatsCompetitionEleve(Long eleveId, LocalDate debut, LocalDate fin) {
        List<StatResultat> resultats = (debut != null && fin != null)
                ? resultatRepo.findByEleveIdAndCompetitionDateBetween(eleveId, debut, fin)
                : resultatRepo.findByEleveId(eleveId);
        return resultats.stream()
                .map(r -> new ResultatStatResponse(r.getId(), r.getCompetitionId(), r.getEleveId(), r.getEnseignantId(), r.getNote(), r.getCompetitionDate()))
                .toList();
    }
}
