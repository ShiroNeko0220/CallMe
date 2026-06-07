package fr.miage.toulouse.callme.statistiquesms.service;

import fr.miage.toulouse.callme.libcommun.ApiException;
import fr.miage.toulouse.callme.statistiquesms.DTO.*;
import fr.miage.toulouse.callme.statistiquesms.clients.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatistiquesService {
    private final CoursClient coursClient;
    private final PresenceClient presenceClient;
    private final UtilisateurClient utilisateurClient;
    private final CompetitionClient competitionClient;

    public StatistiquesService(CoursClient coursClient, PresenceClient presenceClient, UtilisateurClient utilisateurClient, CompetitionClient competitionClient) {
        this.coursClient = coursClient;
        this.presenceClient = presenceClient;
        this.utilisateurClient = utilisateurClient;
        this.competitionClient = competitionClient;
    }

    public CoursStatistiqueResponse statistiquesCours() {
        List<CoursClient.CoursResponse> cours = coursClient.listerCours();
        long nombreCours = cours.size();
        if (nombreCours == 0) return new CoursStatistiqueResponse(0, 0.0);

        long totalPresences = cours.stream()
                .mapToLong(c -> Optional.ofNullable(presenceClient.compterParCours(c.getId())).orElse(0L))
                .sum();
        return new CoursStatistiqueResponse(nombreCours, (double) totalPresences / nombreCours);
    }

    public List<PresenceClient.PresenceResponse> elevesPresentsCours(Long idCours) {
        return presenceClient.listerParCours(idCours);
    }

    public List<CoursPresenceEleveResponse> coursPourEleveAvecPresence(Long eleveId, LocalDate debut, LocalDate fin) {
        Integer niveau = utilisateurClient.getNiveauUtilisateur(eleveId);
        if (niveau == null) throw new ApiException(HttpStatus.NOT_FOUND, "Élève introuvable");

        List<CoursClient.CoursResponse> coursNiveau = coursClient.listerParNiveau(niveau);
        List<PresenceClient.PresenceResponse> presences = presenceClient.listerParEleve(eleveId, debut, fin);
        Set<Long> coursPresents = presences.stream().map(PresenceClient.PresenceResponse::getIdCours).collect(Collectors.toSet());

        return coursNiveau.stream()
                .filter(c -> debut == null || !c.getDate().isBefore(debut))
                .filter(c -> fin == null || !c.getDate().isAfter(fin))
                .map(c -> new CoursPresenceEleveResponse(c.getId(), c.getTitre(), c.getDate(), c.getHeureDebut(), c.getNiveauCible(), coursPresents.contains(c.getId())))
                .toList();
    }

    public Long nombreCompetitionsParNiveau(int niveau) {
        return competitionClient.compterParNiveau(niveau);
    }

    public List<CompetitionClient.ResultatResponse> resultatsCompetitionEleve(Long eleveId, LocalDate debut, LocalDate fin) {
        return competitionClient.listerResultatsPourEleve(eleveId, debut, fin);
    }
}
