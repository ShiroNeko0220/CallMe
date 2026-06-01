package service;

import DTO.*;
import clients.UtilisateurClient;
import entity.*;
import fr.miage.toulouse.callme.libcommun.Role;
import repository.*;
import fr.miage.toulouse.callme.libcommun.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CompetitionService {

    private final CompetitionRepository competitionRepo;
    private final ResultatRepository resultatRepo;
    private final UtilisateurClient utilisateurClient;

    public CompetitionService(CompetitionRepository competitionRepo, ResultatRepository resultatRepo, UtilisateurClient utilisateurClient) {
        this.competitionRepo = competitionRepo;
        this.resultatRepo = resultatRepo;
        this.utilisateurClient = utilisateurClient;
    }

    public Competition creer(CompetitionRequest request) {
        verifierNiveau(request.getNiveauCible());
        verifierDate(request.getDate());

        Boolean apte = utilisateurClient.enseignantApte(request.getEnseignantId(), request.getNiveauCible());

        if (!Boolean.TRUE.equals(apte)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Enseignant non apte pour ce niveau");
        }

        Competition competition = new Competition();
        competition.setTitre(request.getTitre());
        competition.setNiveauCible(request.getNiveauCible());
        competition.setDate(request.getDate());
        competition.setHeureDebut(request.getHeureDebut());
        competition.setDuree(request.getDuree());
        competition.setLieu(request.getLieu());
        competition.setEnseignantId(request.getEnseignantId());

        return competitionRepo.save(competition);
    }

    public Competition consulter(Long id) {
        return competitionRepo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Competition non existant"));
    }

    public List<Competition> lister() {
        return competitionRepo.findAll();
    }

    public List<Competition> listerParNiveau(int niveau) {
        verifierNiveau(niveau);
        return competitionRepo.findByNiveauCible(niveau);
    }

    public List<Competition> listerParEnseignant(Long enseignantId) {
        return competitionRepo.findByEnseignantId(enseignantId);
    }

    public List<Competition> listerPourEleve(Long eleveId) {
        Integer niveau = utilisateurClient.getNiveauUtilisateur(eleveId);
        return competitionRepo.findByNiveauCible(niveau);
    }

    public Resultat ajouterResultat(ResultatRequest request) {
        Competition competition = consulter(request.getCompetitionId());

        verifierNote(request.getNote());

        Boolean apte = utilisateurClient.enseignantApte(
                request.getEnseignantId(),
                competition.getNiveauCible()
        );

        if (!Boolean.TRUE.equals(apte)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Seul un enseignant apte peut saisir ce résultat");
        }

        Integer niveauEleve = utilisateurClient.getNiveauUtilisateur(request.getEleveId());

        if (niveauEleve == null || niveauEleve != competition.getNiveauCible()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "L'élève n'appartient pas au niveau de cette compétition");
        }

        resultatRepo.findByCompetitionIdAndEleveId(
                request.getCompetitionId(),
                request.getEleveId()
        ).ifPresent(r -> {
            throw new ApiException(HttpStatus.CONFLICT, "Résultat déjà saisi pour cet élève");
        });

        Resultat resultat = new Resultat();
        resultat.setCompetition(competition);
        resultat.setEleveId(request.getEleveId());
        resultat.setEnseignantId(request.getEnseignantId());
        resultat.setNote(request.getNote());

        return resultatRepo.save(resultat);
    }

    public List<Resultat> listerResultatsParCompetition(Long competitionId) {
        consulter(competitionId);
        return resultatRepo.findByCompetitionId(competitionId);
    }

    public List<Resultat> listerResultatsPourEleve(Long eleveId) {
        return resultatRepo.findByEleveId(eleveId);
    }

    public List<Resultat> listerResultatsPourEleveSurPeriode(
            Long eleveId,
            LocalDate debut,
            LocalDate fin
    ) {
        return resultatRepo.findByEleveIdAndCompetitionDateBetween(eleveId, debut, fin);
    }

    public long compterParNiveau(int niveau) {
        verifierNiveau(niveau);
        return competitionRepo.countByNiveauCible(niveau);
    }

    public void supprimer(Long id) {
        Competition competition = consulter(id);
        competitionRepo.delete(competition);
    }

    private void verifierNiveau(Integer niveau) {
        if (niveau == null || niveau < 1 || niveau > 5) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Niveau invalide entre 1 et 5");
        }
    }

    private void verifierDate(LocalDate date) {
        if (date == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Date obligatoire");
        }

        LocalDate dateMin = LocalDate.now().plusDays(7);

        if (!date.isAfter(dateMin)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La date doit être supérieure à 7 jours calendaires");
        }
    }

    private void verifierNote(BigDecimal note) {
        if (note == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Note obligatoire");
        }

        if (note.compareTo(BigDecimal.ZERO) < 0 || note.compareTo(BigDecimal.TEN) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La note doit être comprise entre 0 et 10");
        }

        if (note.scale() > 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La note doit avoir une précision maximale au dixième");
        }
    }
}
