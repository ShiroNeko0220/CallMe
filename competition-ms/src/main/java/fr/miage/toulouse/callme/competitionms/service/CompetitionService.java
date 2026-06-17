package fr.miage.toulouse.callme.competitionms.service;

import fr.miage.toulouse.callme.competitionms.DTO.CompetitionRequest;
import fr.miage.toulouse.callme.competitionms.DTO.CompetitionResponse;
import fr.miage.toulouse.callme.competitionms.DTO.ResultatRequest;
import fr.miage.toulouse.callme.competitionms.DTO.ResultatResponse;
import fr.miage.toulouse.callme.competitionms.clients.UtilisateurClient;
import fr.miage.toulouse.callme.competitionms.config.RabbitMQConfig;
import fr.miage.toulouse.callme.competitionms.entity.Competition;
import fr.miage.toulouse.callme.competitionms.entity.Resultat;
import fr.miage.toulouse.callme.competitionms.repository.CompetitionRepository;
import fr.miage.toulouse.callme.competitionms.repository.ResultatRepository;
import fr.miage.toulouse.callme.libcommun.ApiException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class CompetitionService {

    private final CompetitionRepository competitionRepo;
    private final ResultatRepository resultatRepo;
    private final UtilisateurClient utilisateurClient;
    private final RabbitTemplate rabbitTemplate;

    public CompetitionService(CompetitionRepository competitionRepo, ResultatRepository resultatRepo,
                              UtilisateurClient utilisateurClient, RabbitTemplate rabbitTemplate) {
        this.competitionRepo = competitionRepo;
        this.resultatRepo = resultatRepo;
        this.utilisateurClient = utilisateurClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public CompetitionResponse creer(CompetitionRequest request, String roleConnecte, Long utilisateurConnecteId) {
        if ("ENSEIGNANT".equals(roleConnecte)) {
            if (utilisateurConnecteId == null || !utilisateurConnecteId.equals(request.getEnseignantId())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Un enseignant ne peut créer que ses propres compétitions");
            }
        }

        verifierNiveau(request.getNiveauCible());
        verifierDate(request.getDate());
        verifierEnseignantApte(request.getEnseignantId(), request.getNiveauCible());

        Competition competition = new Competition();
        competition.setTitre(request.getTitre());
        competition.setNiveauCible(request.getNiveauCible());
        competition.setDate(request.getDate());
        competition.setHeureDebut(request.getHeureDebut());
        competition.setDuree(request.getDuree());
        competition.setLieu(request.getLieu());
        competition.setEnseignantId(request.getEnseignantId());

        Competition saved = competitionRepo.save(competition);
        publierCompetition(saved);
        return toDTO(saved);
    }

    private Competition findById(String id) {
        return competitionRepo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Competition non existante"));
    }

    private Resultat findResultatById(String id) {
        return resultatRepo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Résultat non existant"));
    }

    @Transactional(readOnly = true)
    public CompetitionResponse consulter(String id) {
        return toDTO(findById(id));
    }

    @Transactional(readOnly = true)
    public List<CompetitionResponse> lister() {
        return competitionRepo.findAll().stream().map(this::toDTO).toList();
    }

    public List<CompetitionResponse> listerParNiveau(int niveau) {
        verifierNiveau(niveau);
        return competitionRepo.findByNiveauCible(niveau).stream().map(this::toDTO).toList();
    }

    public List<CompetitionResponse> listerParEnseignant(Long enseignantId) {
        return competitionRepo.findByEnseignantId(enseignantId).stream().map(this::toDTO).toList();
    }

    public List<CompetitionResponse> listerPourEleve(Long eleveId) {
        Integer niveau = utilisateurClient.getNiveauUtilisateur(eleveId);
        if (niveau == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Élève non existant");
        }
        return competitionRepo.findByNiveauCible(niveau).stream().map(this::toDTO).toList();
    }

    @Transactional
    public CompetitionResponse modifier(String id, CompetitionRequest request, String roleConnecte, Long utilisateurConnecteId) {
        Competition competition = findById(id);
        verifierDroitCompetition(competition, roleConnecte, utilisateurConnecteId, "modifier");

        if (request.getTitre() != null) {
            competition.setTitre(request.getTitre());
        }
        if (request.getNiveauCible() != null) {
            verifierNiveau(request.getNiveauCible());
            competition.setNiveauCible(request.getNiveauCible());
        }
        if (request.getDate() != null) {
            verifierDate(request.getDate());
            competition.setDate(request.getDate());
        }
        if (request.getHeureDebut() != null) {
            competition.setHeureDebut(request.getHeureDebut());
        }
        if (request.getDuree() != null) {
            verifierDuree(request.getDuree());
            competition.setDuree(request.getDuree());
        }
        if (request.getLieu() != null) {
            competition.setLieu(request.getLieu());
        }
        if (request.getEnseignantId() != null) {
            if ("ENSEIGNANT".equals(roleConnecte) && !request.getEnseignantId().equals(utilisateurConnecteId)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Un enseignant ne peut pas transférer sa compétition à un autre enseignant");
            }
            competition.setEnseignantId(request.getEnseignantId());
        }

        verifierEnseignantApte(competition.getEnseignantId(), competition.getNiveauCible());
        Competition saved = competitionRepo.save(competition);
        publierCompetition(saved);
        return toDTO(saved);
    }

    @Transactional
    public ResultatResponse ajouterResultat(String competitionId, String roleConnecte, Long utilisateurConnecteId, ResultatRequest request) {
        Competition competition = findById(competitionId);
        verifierDroitCompetition(competition, roleConnecte, utilisateurConnecteId, "saisir un résultat pour");
        verifierNote(request.getNote());

        Integer niveauEleve = utilisateurClient.getNiveauUtilisateur(request.getEleveId());
        if (niveauEleve == null || niveauEleve != competition.getNiveauCible()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "L'élève n'appartient pas au niveau de cette compétition");
        }

        resultatRepo.findByCompetitionIdAndEleveId(competitionId, request.getEleveId())
                .ifPresent(r -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Résultat déjà saisi pour cet élève");
                });

        Resultat resultat = new Resultat();
        resultat.setCompetitionId(competition.getId());
        resultat.setCompetitionDate(competition.getDate());
        resultat.setEleveId(request.getEleveId());
        resultat.setEnseignantId(competition.getEnseignantId());
        resultat.setNote(request.getNote());

        Resultat saved = resultatRepo.save(resultat);
        publierResultat(saved);
        return toResultatDTO(saved);
    }

    @Transactional
    public ResultatResponse modifierResultat(String resultatId, String roleConnecte, Long utilisateurConnecteId, ResultatRequest request) {
        Resultat resultat = findResultatById(resultatId);
        Competition competition = findById(resultat.getCompetitionId());
        verifierDroitCompetition(competition, roleConnecte, utilisateurConnecteId, "modifier un résultat pour");

        if (request.getNote() != null) {
            verifierNote(request.getNote());
            resultat.setNote(request.getNote());
        }

        if (request.getEleveId() != null && !request.getEleveId().equals(resultat.getEleveId())) {
            Integer niveauEleve = utilisateurClient.getNiveauUtilisateur(request.getEleveId());
            if (niveauEleve == null || niveauEleve != competition.getNiveauCible()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "L'élève n'appartient pas au niveau de cette compétition");
            }
            resultatRepo.findByCompetitionIdAndEleveId(competition.getId(), request.getEleveId())
                    .filter(r -> !r.getId().equals(resultat.getId()))
                    .ifPresent(r -> {
                        throw new ApiException(HttpStatus.CONFLICT, "Résultat déjà saisi pour cet élève");
                    });
            resultat.setEleveId(request.getEleveId());
        }

        resultat.setCompetitionDate(competition.getDate());
        resultat.setEnseignantId(competition.getEnseignantId());

        Resultat saved = resultatRepo.save(resultat);
        publierResultat(saved);
        return toResultatDTO(saved);
    }

    public List<ResultatResponse> listerResultatsParCompetition(String competitionId) {
        findById(competitionId);
        return resultatRepo.findByCompetitionId(competitionId).stream().map(this::toResultatDTO).toList();
    }

    private List<ResultatResponse> listerResultatsPourEleve(Long eleveId) {
        return resultatRepo.findByEleveId(eleveId).stream().map(this::toResultatDTO).toList();
    }

    public List<ResultatResponse> listerResultatsPourEleveSurPeriode(Long eleveId, LocalDate debut, LocalDate fin) {
        if (debut != null && fin != null) {
            return resultatRepo.findByEleveIdAndCompetitionDateBetween(eleveId, debut, fin)
                    .stream().map(this::toResultatDTO).toList();
        }
        if (debut != null) {
            return resultatRepo.findByEleveIdAndCompetitionDateGreaterThanEqual(eleveId, debut)
                    .stream().map(this::toResultatDTO).toList();
        }
        if (fin != null) {
            return resultatRepo.findByEleveIdAndCompetitionDateLessThanEqual(eleveId, fin)
                    .stream().map(this::toResultatDTO).toList();
        }
        return listerResultatsPourEleve(eleveId);
    }

    private CompetitionResponse toDTO(Competition c) {
        return CompetitionResponse.builder()
                .id(c.getId())
                .titre(c.getTitre())
                .niveauCible(c.getNiveauCible())
                .date(c.getDate())
                .heureDebut(c.getHeureDebut())
                .duree(c.getDuree())
                .lieu(c.getLieu())
                .enseignantId(c.getEnseignantId())
                .build();
    }

    private ResultatResponse toResultatDTO(Resultat r) {
        return ResultatResponse.builder()
                .id(r.getId())
                .competitionId(r.getCompetitionId())
                .eleveId(r.getEleveId())
                .enseignantId(r.getEnseignantId())
                .note(r.getNote())
                .competitionDate(r.getCompetitionDate())
                .build();
    }

    public long compterParNiveau(int niveau) {
        verifierNiveau(niveau);
        return competitionRepo.countByNiveauCible(niveau);
    }

    public void supprimer(String id) {
        Competition competition = findById(id);
        competitionRepo.delete(competition);
    }

    private void verifierDroitCompetition(Competition competition, String roleConnecte, Long utilisateurConnecteId, String action) {
        if ("PRESIDENT".equals(roleConnecte)) {
            return;
        }
        if ("ENSEIGNANT".equals(roleConnecte)
                && utilisateurConnecteId != null
                && utilisateurConnecteId.equals(competition.getEnseignantId())) {
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN,
                "Seul le président ou l'enseignant responsable peut " + action + " cette compétition");
    }

    private void verifierEnseignantApte(Long enseignantId, Integer niveauCible) {
        Boolean apte = utilisateurClient.enseignantApte(enseignantId, niveauCible);
        if (!Boolean.TRUE.equals(apte)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Enseignant non apte pour ce niveau");
        }
    }

    private void publierCompetition(Competition saved) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.KEY_COMPETITION, Map.of(
                "id", saved.getId(),
                "titre", saved.getTitre(),
                "niveauCible", saved.getNiveauCible(),
                "date", saved.getDate().toString(),
                "enseignantId", saved.getEnseignantId()
        ));
    }

    private void publierResultat(Resultat saved) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.KEY_RESULTAT, Map.of(
                "id", saved.getId(),
                "competitionId", saved.getCompetitionId(),
                "eleveId", saved.getEleveId(),
                "enseignantId", saved.getEnseignantId(),
                "note", saved.getNote().toString(),
                "competitionDate", saved.getCompetitionDate().toString()
        ));
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

    private void verifierDuree(Integer duree) {
        if (duree == null || duree < 45) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Durée invalide (45min minimum)");
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
